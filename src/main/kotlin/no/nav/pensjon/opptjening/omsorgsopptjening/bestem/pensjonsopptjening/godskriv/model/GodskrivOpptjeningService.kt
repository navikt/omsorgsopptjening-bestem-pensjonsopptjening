package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external.PoppClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Component
class GodskrivOpptjeningService(
    private val client: PoppClient,
    private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
    private val behandlingRepo: BehandlingRepo,
    private val oppgaveService: OppgaveService,
    private val transactionTemplate: TransactionTemplate,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprett(godskrivOpptjening: GodskrivOpptjening): GodskrivOpptjening {
        return godskrivOpptjeningRepo.persist(godskrivOpptjening)
    }

    fun process(): GodskrivOpptjening? {
        return transactionTemplate.execute {
            godskrivOpptjeningRepo.finnNesteUprosesserte()?.let { godskrivOpptjening ->
                Mdc.scopedMdc(godskrivOpptjening.correlationId) {
                    Mdc.scopedMdc(godskrivOpptjening.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                behandlingRepo.finn(godskrivOpptjening.behandlingId).let { behandling ->
                                    godskrivOpptjening.ferdig().also {
                                        godskrivOpptjeningRepo.updateStatus(it)
                                        log.info("Lagrer i popp")
                                        client.lagre(
                                            omsorgsyter = behandling.omsorgsyter,
                                            omsorgsÅr = behandling.omsorgsAr,
                                            omsorgstype = behandling.omsorgstype,
                                            kilde = behandling.kilde(),
                                            omsorgsmottaker = behandling.omsorgsmottaker,
                                        )
                                        log.info("Lagret i popp")
                                    }
                                }
                            }
                        } catch (ex: Throwable) {
                            transactionTemplate.execute {
                                godskrivOpptjening.retry(ex.toString()).let { retry ->
                                    retry.opprettOppgave()?.let {
                                        log.error("Gir opp videre prosessering av godskriv opptjening")
                                        oppgaveService.opprett(it)
                                    }
                                    godskrivOpptjeningRepo.updateStatus(retry)
                                }
                            }
                            throw ex
                        }
                    }
                }
            }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException

@Component
class GodskrivOpptjeningService(
    @Qualifier("godskrivOpptjening") private val godskrivClient: GodskrivOpptjeningClient,
    private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
    private val behandlingRepo: BehandlingRepo,
    private val oppgaveService: OppgaveService,
    private val transactionTemplate: TransactionTemplate,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprett(godskrivOpptjening: GodskrivOpptjening.Transient): GodskrivOpptjening.Persistent {
        return godskrivOpptjeningRepo.persist(godskrivOpptjening)
    }

    fun process(): List<GodskrivOpptjening.Persistent>? {
        val låsteGodskrivOpptjeninger = transactionTemplate.execute {
            godskrivOpptjeningRepo.finnNesteUprosesserte(10)
        }!!

        return try {
            låsteGodskrivOpptjeninger.data.mapNotNull { godskrivOpptjening ->
                Mdc.scopedMdc(godskrivOpptjening.correlationId) {
                    Mdc.scopedMdc(godskrivOpptjening.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                behandlingRepo.finn(godskrivOpptjening.behandlingId).let { behandling ->
                                    godskrivOpptjening.ferdig().also {
                                        godskrivClient.godskriv(
                                            omsorgsyter = behandling.omsorgsyter,
                                            omsorgsÅr = behandling.omsorgsAr,
                                            omsorgstype = behandling.omsorgstype,
                                            omsorgsmottaker = behandling.omsorgsmottaker,
                                        )
                                        godskrivOpptjeningRepo.updateStatus(it)
                                    }
                                }
                            }
                        } catch (ex: SQLException) {
                            log.error("Feil under prosessering av godskriv opptjening", ex)
                            throw ex
                        } catch (ex: Throwable) {
                            try {
                                transactionTemplate.execute {
                                    godskrivOpptjening.retry(ex.stackTraceToString()).let { retry ->
                                        retry.opprettOppgave()?.let {
                                            log.error("Gir opp videre prosessering av godskriv opptjening")
                                            oppgaveService.opprett(it)
                                        }
                                        godskrivOpptjeningRepo.updateStatus(retry)
                                    }
                                }
                            } catch (ex: Throwable) {
                                log.error("Feil ved setting av feilstatus: ${ex::class.qualifiedName}")
                            }
                            null
                        }
                    }
                }
            }
        } finally {
            godskrivOpptjeningRepo.frigi(låsteGodskrivOpptjeninger)
        }
    }
}
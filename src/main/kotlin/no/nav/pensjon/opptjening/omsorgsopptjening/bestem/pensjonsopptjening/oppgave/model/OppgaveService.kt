package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.OppgaveKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Component
class OppgaveService(
    private val sakKlient: BestemSakKlient,
    private val oppgaveKlient: OppgaveKlient,
    private val oppgaveRepo: OppgaveRepo,
    private val personOppslag: PersonOppslag,
    private val transactionTemplate: TransactionTemplate
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprett(oppgave: Oppgave.Transient): Oppgave {
        return oppgaveRepo.persist(oppgave)
    }

    fun oppgaveEksistererForOmsorgsyterOgÅr(omsorgsyter: String, år: Int): Boolean {
        return oppgaveRepo.existsForOmsorgsyterOgÅr(omsorgsyter, år)
    }

    fun oppgaveEksistererForOmsorgsmottakerOgÅr(omsorgsmottaker: String, år: Int): Boolean {
        return oppgaveRepo.existsForOmsorgsmottakerOgÅr(omsorgsmottaker, år)
    }

    fun process(): Oppgave? {
        return transactionTemplate.execute {
            oppgaveRepo.finnNesteUprosesserte()?.let { oppgave ->
                Mdc.scopedMdc(oppgave.correlationId) {
                    Mdc.scopedMdc(oppgave.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                log.info("Oppretter oppgave")
                                personOppslag.hentAktørId(oppgave.mottaker).let { aktørId ->
                                    sakKlient.bestemSak(
                                        aktørId = aktørId
                                    ).let { omsorgssak ->
                                        oppgaveKlient.opprettOppgave(
                                            aktoerId = aktørId,
                                            sakId = omsorgssak.sakId,
                                            beskrivelse = oppgave.oppgavetekst,
                                            tildeltEnhetsnr = omsorgssak.enhet
                                        ).let { oppgaveId ->
                                            oppgave.ferdig(oppgaveId).also {
                                                oppgaveRepo.updateStatus(it)
                                                log.info("Oppgave opprettet")
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ex: Throwable) {
                            transactionTemplate.execute {
                                oppgave.retry(ex.stackTraceToString()).let {
                                    if (it.status is Oppgave.Status.Feilet) {
                                        log.error("Gir opp videre prosessering av oppgave")
                                    }
                                    oppgaveRepo.updateStatus(it)
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
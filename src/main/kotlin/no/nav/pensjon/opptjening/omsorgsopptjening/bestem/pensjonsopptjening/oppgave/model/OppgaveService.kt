package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysning
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
    fun opprett(oppgave: Oppgave.Transient): Oppgave.Persistent {
        return oppgaveRepo.persist(oppgave)
    }

    fun oppgaveEksistererForOmsorgsyterOgÅr(omsorgsyter: String, år: Int): Boolean {
        return oppgaveRepo.existsForOmsorgsyterOgÅr(omsorgsyter, år)
    }

    fun oppgaveEksistererForOmsorgsmottakerOgÅr(omsorgsmottaker: String, år: Int): Boolean {
        return oppgaveRepo.existsForOmsorgsmottakerOgÅr(omsorgsmottaker, år)
    }

    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprettOppgaveHvisNødvendig(behandling: FullførtBehandling) {
        behandling.hentOppgaveopplysninger().let { oppgaveopplysning ->
            when (oppgaveopplysning) {
                is Oppgaveopplysning.ToOmsorgsytereMedLikeMangeMånederOmsorg -> {
                    if (behandling.omsorgsmottakerFødtIOmsorgsår()) {
                        OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
                            omsorgsyter = oppgaveopplysning.oppgaveMottaker,
                            omsorgsmottaker = oppgaveopplysning.omsorgsmottaker,
                        )
                    } else {
                        OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg(
                            omsorgsyter = oppgaveopplysning.oppgaveMottaker,
                            omsorgsmottaker = oppgaveopplysning.omsorgsmottaker,
                            annenOmsorgsyter = oppgaveopplysning.annenOmsorgsyter,
                        )
                    }.let {
                        val omsorgsyterHarOppgaveForÅr =
                            oppgaveEksistererForOmsorgsyterOgÅr(
                                oppgaveopplysning.oppgaveMottaker,
                                oppgaveopplysning.omsorgsår
                            )
                        val omsorgsMottakerHarOppgaveForÅr =
                            oppgaveEksistererForOmsorgsmottakerOgÅr(
                                oppgaveopplysning.omsorgsmottaker,
                                oppgaveopplysning.omsorgsår
                            )

                        if (!omsorgsyterHarOppgaveForÅr && !omsorgsMottakerHarOppgaveForÅr) {
                            opprett(
                                Oppgave.Transient(
                                    detaljer = it,
                                    behandlingId = behandling.id,
                                    meldingId = behandling.meldingId,
                                )
                            )
                        }
                    }
                }

                Oppgaveopplysning.Ingen -> {
                    //noop
                }
            }

        }
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
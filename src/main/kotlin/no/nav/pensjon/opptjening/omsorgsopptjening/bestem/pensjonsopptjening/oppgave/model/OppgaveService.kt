package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.OppgaveKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.apache.kafka.common.KafkaException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException

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
        val omsorgsMottakerHarOppgaveForÅr =
            oppgaveEksistererForOmsorgsmottakerOgÅr(
                behandling.omsorgsmottaker,
                behandling.omsorgsAr
            )

        fun oppgavemottakerHarOppgaveForÅr(oppgaveopplysning: Oppgaveopplysninger.Generell): Boolean {
            return oppgaveEksistererForOmsorgsyterOgÅr(
                oppgaveopplysning.oppgavemottaker,
                behandling.omsorgsAr
            )
        }
        if (!omsorgsMottakerHarOppgaveForÅr) {
            behandling.hentOppgaveopplysninger()
                .filterIsInstance<Oppgaveopplysninger.Generell>()
                .filterNot { oppgaveopplysning -> oppgavemottakerHarOppgaveForÅr(oppgaveopplysning) }
                .groupBy { it.oppgavemottaker }
                .mapValues { o -> o.value.map { it.oppgaveTekst }.toSet() }
                .forEach { (oppgavemottaker, oppgaveTekster) ->
                    //TODO legg alle oppgavetekster for den samme behandlingen i en og samme oppgave
                    opprett(
                        Oppgave.Transient(
                            behandlingId = behandling.id,
                            meldingId = behandling.meldingId,
                            detaljer = OppgaveDetaljer.MottakerOgTekst(
                                oppgavemottaker = oppgavemottaker,
                                oppgavetekst = oppgaveTekster
                            )
                        )
                    )
                }
        }
    }

    fun process(): List<Oppgave>? {
        return transactionTemplate.execute {
            oppgaveRepo.finnNesteUprosesserte(10).mapNotNull { oppgave ->
                Mdc.scopedMdc(oppgave.correlationId) {
                    Mdc.scopedMdc(oppgave.innlesingId) {
                        try {
                            personOppslag.hentAktørId(oppgave.mottaker).let { aktørId ->
                                sakKlient.bestemSak(
                                    aktørId = aktørId
                                ).let { omsorgssak ->
                                    oppgaveKlient.opprettOppgave(
                                        aktoerId = aktørId,
                                        sakId = omsorgssak.sakId,
                                        // TODO: Skal ikke kunne være tom
                                        beskrivelse = FlereOppgaveteksterFormatter.format(oppgave.oppgavetekst),
                                        tildeltEnhetsnr = omsorgssak.enhet
                                    ).let { oppgaveId ->
                                        oppgave.ferdig(oppgaveId).also {
                                            oppgaveRepo.updateStatus(it)
                                            log.info("Oppgave opprettet")
                                        }
                                    }
                                }
                            }
                        } catch (ex: SQLException) {
                            log.error("Feil ved prosessering av oppgaver", ex)
                            throw ex
                        } catch (ex: Throwable) {
                            oppgave.retry(ex.stackTraceToString()).let {
                                if (it.status is Oppgave.Status.Feilet) {
                                    log.error("Gir opp videre prosessering av oppgave")
                                }
                                oppgaveRepo.updateStatus(it)
                                null
                            }
                        }
                    }
                }
            }
        }
    }
}
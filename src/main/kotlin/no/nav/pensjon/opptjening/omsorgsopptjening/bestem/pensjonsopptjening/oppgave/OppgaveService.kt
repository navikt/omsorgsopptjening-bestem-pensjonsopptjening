package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OppgaveService(
    private val sakKlient: BestemSakKlient,
    private val oppgaveKlient: OppgaveKlient,
    private val oppgaveRepo: OppgaveRepo,
    private val pdlService: PdlService,
) {
    @Autowired
    private lateinit var statusoppdatering: Statusoppdatering

    /**
     * https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
     *
     * "In proxy mode (which is the default), only external method calls coming in through the proxy are intercepted.
     * This means that self-invocation (in effect, a method within the target object calling another method of the target object)
     * does not lead to an actual transaction at runtime even if the invoked method is marked with @Transactional.
     * Also, the proxy must be fully initialized to provide the expected behavior, so you should not rely on this feature
     * in your initialization code - for example, in a @PostConstruct method."
     */
    @Component
    private class Statusoppdatering(
        private val oppgaveRepo: OppgaveRepo,
    ) {
        @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRES_NEW)
        fun markerForRetry(oppgave: Oppgave, exception: Throwable) {
            oppgave.retry(exception.toString()).let {
                if (it.status is Oppgave.Status.Feilet) {
                    log.error("Gir opp videre prosessering av oppgave")
                }
                oppgaveRepo.updateStatus(it)
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun opprett(oppgave: Oppgave): Oppgave {
        return oppgaveRepo.persist(oppgave)
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun opprett(melding: OmsorgsarbeidMelding): Oppgave {
        log.info("Lagrer oppgavebestilling")
        return deserialize<OmsorgsgrunnlagMelding>(melding.melding).let {
            oppgaveRepo.persist(
                Oppgave(
                    detaljer = OppgaveDetaljer.UspesifisertFeilsituasjon(
                        omsorgsyter = it.omsorgsyter,
                    ),
                    behandlingId = null,
                    meldingId = melding.id!!
                )
            )
        }
    }

    fun oppgaveEksistererForOmsorgsyterOgÅr(omsorgsyter: String, år: Int): Boolean {
        return oppgaveRepo.existsForOmsorgsyterOgÅr(omsorgsyter, år)
    }

    fun oppgaveEksistererForOmsorgsmottakerOgÅr(omsorgsmottaker: String, år: Int): Boolean {
        return oppgaveRepo.existsForOmsorgsmottakerOgÅr(omsorgsmottaker, år)
    }


    @Transactional(rollbackFor = [Throwable::class])
    fun process(): Oppgave? {
        return oppgaveRepo.finnNesteUprosesserte()?.let { oppgave ->
            Mdc.scopedMdc(CorrelationId.name, oppgave.correlationId.toString()) {
                log.info("Oppretter oppgave")
                try {
                    pdlService.hentAktorId(oppgave.mottaker).let { aktørId ->
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
                } catch (exception: Throwable) {
                    log.warn("Exception caught while processing oppgave: ${oppgave.id}, exeption:$exception")
                    statusoppdatering.markerForRetry(oppgave, exception)
                    throw exception
                }
            }
        }
    }
}
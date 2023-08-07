package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.PersistertKafkaMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BarnetrygdGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
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
    fun opprett(behandling: FullførtBehandling): Oppgave {
        //TODO hadde desember ref oppgavetekst/batch
        log.info("Lagrer oppgavebestilling")
        return when (behandling.grunnlag) {
            is BarnetrygdGrunnlag.FødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
                    omsorgsyter = behandling.omsorgsyter,
                    omsorgsmottaker = behandling.omsorgsmottaker,
                )
            }

            is BarnetrygdGrunnlag.IkkeFødtIOmsorgsår -> {
                OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg(
                    omsorgsyter = behandling.omsorgsyter,
                    omsorgsmottaker = behandling.omsorgsmottaker,
                    annenOmsorgsyter = behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().grunnlag.andreOmsorgsytereMedLikeMange().keys.first()
                )
            }
        }.let {
            oppgaveRepo.persist(
                Oppgave(
                    detaljer = it,
                    behandlingId = behandling.id,
                    meldingId = behandling.kafkaMeldingId
                )
            )
        }
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun opprett(melding: PersistertKafkaMelding): Oppgave {
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

    @Transactional(rollbackFor = [Throwable::class])
    fun process(): Oppgave? {
        return oppgaveRepo.finnNesteUprosesserte()?.let { oppgave ->
            Mdc.scopedMdc(CorrelationId.name, oppgave.correlationId.toString()) {
                log.info("Oppretter oppgave")
                try {
//                    sakKlient.kallBestemSak(
//                        requestBody = BestemSakRequest(
//                            aktoerId = "",
//                            ytelseType =,
//                            callId =,
//                            consumerId =
//                        )
//                    )
//                    val id = "" //TODO
//                    oppgaveKlient.opprettOppgave(
//                        aktoerId = "",
//                        sakId = "",
//                        beskrivelse = "",
//                        tildeltEnhetsnr = ""
//                    )
                    oppgave.ferdig("oppgaveId").also {
                        oppgaveRepo.updateStatus(it)
                        log.info("Oppgave opprettet")
                    }
                } catch (exception: Throwable) {
                    log.error("Exception caught while processing oppgave: ${oppgave.id}, exeption:$exception")
                    statusoppdatering.markerForRetry(oppgave, exception)
                    throw exception
                }
            }
        }
    }
}
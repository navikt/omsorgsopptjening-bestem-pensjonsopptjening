package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka.OmsorgsopptjeningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BarnetrygdGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Month

@Service
class OmsorgsarbeidMessageService(
    private val omsorgsgrunnlagService: OmsorgsgrunnlagService,
    private val behandlingRepo: BehandlingRepo,
    private val gyldigOpptjeningsår: GyldigOpptjeningår,
    private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
    private val omsorgsopptjeningProducer: OmsorgsopptjeningProducer,
    private val oppgaveService: OppgaveService
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
        private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
        private val oppgaveService: OppgaveService
    ) {
        @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRES_NEW)
        fun markerForRetry(melding: PersistertKafkaMelding, exception: Throwable) {
            melding.retry(exception.toString()).let {
                if (it.status is PersistertKafkaMelding.Status.Feilet) {
                    log.error("Gir opp videre prosessering av melding")
                    oppgaveService.opprett(melding)
                }
                omsorgsarbeidRepo.updateStatus(it)
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun process(): List<FullførtBehandling> {
        return omsorgsarbeidRepo.finnNesteUprosesserte()?.let { melding ->
            Mdc.scopedMdc(CorrelationId.name, melding.correlationId) {
                try {
                    log.info("Prosesserer melding")
                    handle(melding).also { resultat ->
                        omsorgsarbeidRepo.updateStatus(melding.ferdig())
                        resultat.forEach { //TODO forekle ved å droppe flere år?
                            when (it.erInnvilget()) {
                                true -> {
                                    håndterInnvilgelse(it)
                                }

                                false -> {
                                    håndterAvslag(it)
                                }
                            }
                            log.info("Melding prosessert")
                        }
                    }
                } catch (exception: Throwable) {
                    log.error("Exception caught while processing message: ${melding.id}, exeption:$exception")
                    statusoppdatering.markerForRetry(melding, exception)
                    throw exception
                }
            }
        } ?: emptyList()
    }

    private fun handle(melding: PersistertKafkaMelding): List<FullførtBehandling> {
        return deserialize<OmsorgsgrunnlagMelding>(melding.melding)
            .berik()
            .barnetrygdgrunnlagPerMottakerPerÅr()
            .filter { barnetrygdGrunnlag ->
                gyldigOpptjeningsår.get().contains(barnetrygdGrunnlag.omsorgsAr).also {
                    if (!it) log.info("Filtrerer vekk grunnlag for ugyldig opptjeningsår: ${barnetrygdGrunnlag.omsorgsAr}")
                }
            }
            .map {
                log.info("Utfører vilkårsvurdering")
                behandlingRepo.persist(
                    Behandling(
                        grunnlag = it,
                        vurderVilkår = VilkårsvurderingFactory(
                            grunnlag = it,
                            behandlingRepo = behandlingRepo
                        ),
                        kafkaMeldingId = melding.id!!
                    )
                )
            }
    }


    private fun håndterInnvilgelse(behandling: FullførtBehandling) {
        log.info("Håndterer innvilgelse")
        omsorgsopptjeningProducer.send(behandling)
    }

    private fun håndterAvslag(behandling: FullførtBehandling) {
        log.info("Håndterer avslag")
        //TODO forhindre doble oppgaver? Vil produsere oppgave for alle omsorgsytere p.t.
        when (behandling.skalOppretteOppgave()) {
            true -> {
                oppgaveService.opprett(behandling)
            }
            false -> {
                //NOOP
            }
        }
    }

    private fun OmsorgsgrunnlagMelding.berik(): BeriketDatagrunnlag {
        log.info("Beriker datagrunnlag")
        return omsorgsgrunnlagService.berikDatagrunnlag(this)
    }

    private fun BeriketDatagrunnlag.barnetrygdgrunnlagPerMottakerPerÅr(): List<BarnetrygdGrunnlag> {
        log.info("Lager grunnlag per omsorgsmottaker per opptjeningsår")
        return perMottakerPerÅr().fold(emptyList()) { acc, (mottaker, år, grunnlag) ->
            acc + when (mottaker.erFødt(år)) {
                true -> {
                    require(
                        !mottaker.erFødt(
                            årstall = år,
                            måned = Month.DECEMBER
                        )
                    ) { "Forventer ikke grunnlag for fødselsåret dersom barn er født i desember" }
                    listOf(
                        BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember(
                            omsorgsAr = år,
                            grunnlag = grunnlag
                        )
                    )
                }

                false -> {
                    when (mottaker.erFødt(år - 1, Month.DECEMBER)) {
                        true -> {
                            listOf(
                                BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember(
                                    omsorgsAr = år - 1,
                                    grunnlag = grunnlag
                                ),
                                BarnetrygdGrunnlag.IkkeFødtIOmsorgsår(
                                    omsorgsAr = år,
                                    grunnlag = grunnlag
                                ),
                            )
                        }

                        false -> {
                            listOf(
                                BarnetrygdGrunnlag.IkkeFødtIOmsorgsår(
                                    omsorgsAr = år,
                                    grunnlag = grunnlag
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun BeriketDatagrunnlag.perMottakerPerÅr(): List<Triple<Person, Int, BeriketDatagrunnlag>> {
        return perMottaker().flatMap { (omsorgsmottaker, grunnlagPerMottaker) ->
            grunnlagPerMottaker.perÅr().map { (år, grunnlagPerÅrPerMottaker) ->
                Triple(omsorgsmottaker, år, grunnlagPerÅrPerMottaker)
            }
        }
    }

    private fun BeriketDatagrunnlag.perMottaker(): Map<Person, BeriketDatagrunnlag> {
        return omsorgsmottakere().associateWith { omsorgsmottaker ->
            copy(omsorgsSaker = omsorgsSaker.map { sak -> sak.copy(omsorgVedtakPerioder = sak.omsorgVedtakPerioder.filter { it.omsorgsmottaker == omsorgsmottaker }) })
        }
    }

    private fun BeriketDatagrunnlag.perÅr(): Map<Int, BeriketDatagrunnlag> {
        val alleÅrIGrunnlag = omsorgsSaker.flatMap { it.omsorgVedtakPerioder }
            .flatMap { it.periode.alleMåneder() }
            .map { it.year }
            .distinct()

        return alleÅrIGrunnlag.associateWith { år ->
            copy(omsorgsSaker = omsorgsSaker
                .map { sak ->
                    sak.copy(omsorgVedtakPerioder = sak.omsorgVedtakPerioder
                        .filter { it.periode.overlapper(år) }
                        .map { barnetrygdPeriode ->
                            barnetrygdPeriode.periode.overlappendeMåneder(år).let {
                                BeriketVedtaksperiode(
                                    fom = it.min(),
                                    tom = it.max(),
                                    prosent = barnetrygdPeriode.prosent,
                                    omsorgsmottaker = barnetrygdPeriode.omsorgsmottaker
                                )
                            }
                        })
                })
        }
    }
}
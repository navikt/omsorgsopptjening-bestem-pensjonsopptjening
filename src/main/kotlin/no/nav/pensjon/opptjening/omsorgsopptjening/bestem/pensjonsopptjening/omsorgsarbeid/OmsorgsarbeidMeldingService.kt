package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketDatagrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketVedtaksperiode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.toDomain
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka.OmsorgsopptjeningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.transformerTilBarnetrygdGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
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

@Service
class OmsorgsarbeidMeldingService(
    private val behandlingRepo: BehandlingRepo,
    private val gyldigOpptjeningsår: GyldigOpptjeningår,
    private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
    private val omsorgsopptjeningProducer: OmsorgsopptjeningProducer,
    private val oppgaveService: OppgaveService,
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
        private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
        private val oppgaveService: OppgaveService
    ) {
        @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRES_NEW)
        fun markerForRetry(melding: OmsorgsarbeidMelding, exception: Throwable) {
            melding.retry(exception.toString()).let { melding ->
                melding.opprettOppgave()?.let {
                    log.error("Gir opp videre prosessering av melding")
                    oppgaveService.opprett(it)
                }
                omsorgsarbeidRepo.updateStatus(melding)
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
                        resultat.forEach {
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
                    log.warn("Exception caught while processing message: ${melding.id}, exeption:$exception")
                    statusoppdatering.markerForRetry(melding, exception)
                    throw exception
                }
            }
        } ?: emptyList()
    }

    private fun handle(melding: OmsorgsarbeidMelding): List<FullførtBehandling> {
        return deserialize<OmsorgsgrunnlagMelding>(melding.melding)
            .berikDatagrunnlag()
            .transformerTilBarnetrygdGrunnlag()
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
        behandling.opprettOppgave(
            oppgaveEksistererForOmsorgsyter = oppgaveService::oppgaveEksistererForOmsorgsyterOgÅr,
            oppgaveEksistererForOmsorgsmottaker = oppgaveService::oppgaveEksistererForOmsorgsmottakerOgÅr
        )?.also { oppgaveService.opprett(it) }
    }

    private fun OmsorgsgrunnlagMelding.berikDatagrunnlag(): BeriketDatagrunnlag {
        val personer = hentPersoner().map {
            pdlService.hentPerson(it)
        }.map {
            Person(it.gjeldendeFnr, it.fodselsdato)
        }.toSet()

        return berikDatagrunnlag(personer)
    }

    private fun OmsorgsgrunnlagMelding.berikDatagrunnlag(persondata: Set<Person>): BeriketDatagrunnlag {
        fun Set<Person>.finnPerson(fnr: String): Person {
            return single { it.fnr == fnr }
        }

        return BeriketDatagrunnlag(
            omsorgsyter = persondata.finnPerson(omsorgsyter),
            omsorgstype = omsorgstype.toDomain(),
            kjoreHash = kjoreHash,
            kilde = kilde.toDomain(),
            omsorgsSaker = saker.map { omsorgsSak ->
                BeriketSak(
                    omsorgsyter = persondata.finnPerson(omsorgsSak.omsorgsyter),
                    omsorgVedtakPerioder = omsorgsSak.vedtaksperioder.map { omsorgVedtakPeriode ->
                        BeriketVedtaksperiode(
                            fom = omsorgVedtakPeriode.fom,
                            tom = omsorgVedtakPeriode.tom,
                            prosent = omsorgVedtakPeriode.prosent,
                            omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker)
                        )
                    }

                )
            }
        )
    }
}
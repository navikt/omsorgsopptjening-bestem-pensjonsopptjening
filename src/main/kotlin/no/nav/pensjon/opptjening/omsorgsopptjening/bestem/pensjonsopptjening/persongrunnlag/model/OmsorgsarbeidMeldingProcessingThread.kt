package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class OmsorgsarbeidMeldingProcessingThread(
    private val handler: PersongrunnlagMeldingService,
    private val unleash: Unleash,
    private val metrics: MicrometerMetrics
    ) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-omsorgsarbeid-melding-thread"
        log.info("Starting new thread:$name to process omsorgsarbeid")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                if(unleash.isEnabled(NavUnleashConfig.Feature.BEHANDLING.toggleName)) {
                    metrics.omsorgsarbeidProsessertTidsbruk.recordCallable {
                        val fullførteBehandlinger = handler.process()
                        if(fullførteBehandlinger.any { it.erInnvilget() }) {
                            metrics.innvilget.increment()
                        } else {
                            extracted(fullførteBehandlinger)
                        }
                    }
                }
            } catch (exception: Throwable) {
                metrics.antallFeiledeOmsorgsarbeid.increment()
                metrics.omsorgsarbeidFeiletTidsbruk.recordCallable {
                    log.warn("Exception caught while processing, exception:$exception")
                    Thread.sleep(1000)
                }
            }
        }
    }

    private fun extracted(fullførteBehandlinger: List<FullførtBehandling>) {
        fullførteBehandlinger.flatMap { it.avslagsArsaker() }.associateBy { it.javaClass }.values.map {
            when (it) {
                is EllerVurdering -> {}
                is OgVurdering -> {}
                is OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering -> metrics.omsorgsyterHarMestOmsorgAvAlleOmsorgsytere.increment()
                is OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering -> metrics.omsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.increment()
                is OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering -> metrics.omsorgsyterHarTilstrekkeligOmsorgsarbeid.increment()
                is OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering -> metrics.omsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.increment()
                is OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering -> metrics.omsorgsyterErForelderTilMottakerAvHjelpestønad.increment()
                is OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering -> metrics.omsorgsmottakerOppfyllerAlderskravForHjelpestønad.increment()
                is OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering -> metrics.omsorgsmottakerOppfyllerAlderskravForBarnetrygd.increment()
                is OmsorgsyterOppfyllerAlderskrav.Vurdering -> metrics.omsorgsyterOppfyllerAlderskrav.increment()
            }
        }
        metrics.avslag.increment()
    }
}
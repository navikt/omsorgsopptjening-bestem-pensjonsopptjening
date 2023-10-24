package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetricsMåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class PersongrunnlagMeldingProcessingThread(
    private val handler: PersongrunnlagMeldingService,
    private val unleash: Unleash,
    private val omsorgsarbeidMetricsMåling: OmsorgsarbeidProcessingMetricsMåling,
    private val omsorgsarbeidMetricsFeilmåling: OmsorgsarbeidProcessingMetricsFeilmåling,

    ) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-persongrunnlag-melding-thread"
        log.info("Starting new thread:$name to process persongrunnlag")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.BEHANDLING.toggleName)) {
                    omsorgsarbeidMetricsMåling.mål {
                        handler.process()
                    }
                }
            } catch (exception: Throwable) {
                omsorgsarbeidMetricsFeilmåling.målfeil {
                    log.warn("Exception caught while processing, exception:$exception")
                    Thread.sleep(1000)
                }
            }
        }
    }
}
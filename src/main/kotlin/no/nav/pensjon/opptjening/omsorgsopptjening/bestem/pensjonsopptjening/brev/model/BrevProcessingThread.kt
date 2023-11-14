package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class BrevProcessingThread(
    private val service: BrevService,
    private val unleash: Unleash,
    private val brevProcessingMetricsMåling: BrevProcessingMetrikker,
    private val brevProcessingMetricsFeilmåling: BrevProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-brev-thread"
        log.info("Starting new thread:$name to process brev")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.BREV.toggleName) && datasourceReadinessCheck.isReady()) {
                    brevProcessingMetricsMåling.oppdater { service.process() }
                }
            } catch (exception: Throwable) {
                brevProcessingMetricsFeilmåling.oppdater {
                    log.warn("Exception caught while processing, exception:$exception")
                }
            }
        }
    }
}
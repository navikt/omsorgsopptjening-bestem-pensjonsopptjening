package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.LoggerFactory

class BrevProcessingTask(
    private val service: BrevService,
    private val unleash: UnleashWrapper,
    private val brevProcessingMetricsMåling: BrevProcessingMetrikker,
    private val brevProcessingMetricsFeilmåling: BrevProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }

    init {
        log.info("Starting new thread to process brev")
    }

    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.BREV) && datasourceReadinessCheck.isReady()) {
                    brevProcessingMetricsMåling.oppdater { service.process() }
                }
            } catch (exception: Throwable) {
                brevProcessingMetricsFeilmåling.oppdater {
                    log.warn("Exception caught while processing ${exception::class.qualifiedName}")
                }
            }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.LoggerFactory

class GodskrivOpptjeningProcessingThread(
    private val service: GodskrivOpptjeningProcessingService,
    private val unleash: UnleashWrapper,
    private val godskrivProcessingMetricsMåling: GodskrivProcessingMetrikker,
    private val godskrivProcessingMetricsFeilmåling: GodskrivProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }

    init {
        log.info("Starting new thread to process godskriv opptjening")
    }

    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.GODSKRIV) && datasourceReadinessCheck.isReady()) {
                    godskrivProcessingMetricsMåling.oppdater {
                        service.process()
                    }
                }
            } catch (exception: Throwable) {
                godskrivProcessingMetricsFeilmåling.oppdater {
                    log.warn("Exception caught while processing: ${exception::class.qualifiedName}")
                }
            }
        }
    }
}
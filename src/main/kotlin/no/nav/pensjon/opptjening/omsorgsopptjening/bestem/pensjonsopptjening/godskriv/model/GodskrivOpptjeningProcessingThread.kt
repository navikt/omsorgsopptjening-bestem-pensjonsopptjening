package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory

internal class GodskrivOpptjeningProcessingThread(
    private val service: GodskrivOpptjeningProcessingService,
    private val unleash: Unleash,
    private val godskrivProcessingMetricsMåling: GodskrivProcessingMetrikker,
    private val godskrivProcessingMetricsFeilmåling: GodskrivProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-godskriv-opptjening-thread"
        log.info("Starting new thread:$name to process godskriv opptjening")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.GODSKRIV.toggleName) && datasourceReadinessCheck.isReady()) {
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
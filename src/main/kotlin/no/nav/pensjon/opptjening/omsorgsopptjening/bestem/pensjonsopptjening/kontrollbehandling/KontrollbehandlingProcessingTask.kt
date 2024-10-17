package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.LoggerFactory

class KontrollbehandlingProcessingTask(
    private val service: KontrollbehandlingProcessingService,
    private val unleash: UnleashWrapper,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    init {
        log.info("Starting new thread to process kontrollbehandlinger")
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }


    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.KONTROLL) && datasourceReadinessCheck.isReady()) {
                    service.process() ?: run {
                        Thread.sleep(1000)
                        null
                    }
                }
            } catch (exception: Throwable) {
                log.warn("Exception caught while processing ${exception::class.qualifiedName}")
            }
        }
    }
}
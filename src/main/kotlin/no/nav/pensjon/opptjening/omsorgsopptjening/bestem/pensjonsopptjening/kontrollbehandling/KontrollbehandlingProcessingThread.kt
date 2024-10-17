package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory

class KontrollbehandlingProcessingThread(
    private val service: KontrollbehandlingProcessingService,
    private val unleash: Unleash,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    init {
        val name = "prosesser-persongrunnlag-kontrollbehandling-thread"
        log.info("Starting new thread:$name to process kontrollbehandlinger")
        Thread(this, name).start()
    }


    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }


    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.KONTROLL.toggleName) && datasourceReadinessCheck.isReady()) {
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
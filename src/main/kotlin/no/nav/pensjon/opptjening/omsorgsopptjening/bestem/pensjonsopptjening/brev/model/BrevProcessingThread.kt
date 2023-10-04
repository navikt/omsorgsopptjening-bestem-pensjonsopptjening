package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class BrevProcessingThread(
    private val service: BrevService,
    private val unleash: Unleash,
    private val metrics: MicrometerMetrics,
) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
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
                if (unleash.isEnabled(NavUnleashConfig.Feature.BREV.toggleName)) {
                    metrics.brevProsessertTidsbruk.recordCallable { service.process() }
                }
            } catch (exception: Throwable) {
                metrics.antallFeiledeBrev.increment()
                metrics.brevFeiletTidsbruk.recordCallable {
                    log.warn("Exception caught while processing, exception:$exception")
                    Thread.sleep(1000)
                }
            }
        }
    }
}
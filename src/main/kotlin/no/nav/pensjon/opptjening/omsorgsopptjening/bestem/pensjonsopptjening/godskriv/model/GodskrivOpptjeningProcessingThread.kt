package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class GodskrivOpptjeningProcessingThread(
    private val service: GodskrivOpptjeningService,
    private val unleash: Unleash
) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
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
                if(unleash.isEnabled(NavUnleashConfig.Feature.GODSKRIV_POPP.toggleName)) {
                    service.process()
                }
            } catch (exception: Throwable) {
                log.warn("Exception caught while processing, exception:$exception")
                Thread.sleep(1000)
            }
        }
    }
}
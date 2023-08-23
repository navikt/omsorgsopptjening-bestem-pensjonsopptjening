package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class GodskrivOpptjeningProcessingThread(
    private val service: GodskrivOpptjeningService
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
                service.process()
            } catch (exception: Throwable) {
                log.warn("Exception caught while processing, exception:$exception")
                Thread.sleep(1000)
            }
        }
    }
}
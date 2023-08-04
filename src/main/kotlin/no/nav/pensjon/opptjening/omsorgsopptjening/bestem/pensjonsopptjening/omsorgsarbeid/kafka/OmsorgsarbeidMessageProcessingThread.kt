package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!no-kafka")
class OmsorgsarbeidMessageProcessingThread(
    private val handler: OmsorgsarbeidMessageService
) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-omsorgsarbeid-melding-thread"
        log.info("Starting new thread:$name to process omsorgsarbeid")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                handler.process()
            } catch (exception: Throwable) {
                log.error("Exception caught while processing, exception:$exception")
                Thread.sleep(1000)
            }
        }
    }
}
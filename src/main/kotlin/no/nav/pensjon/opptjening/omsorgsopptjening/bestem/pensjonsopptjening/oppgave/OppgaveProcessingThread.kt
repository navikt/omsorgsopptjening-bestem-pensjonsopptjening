package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!no-kafka")
class OppgaveProcessingThread(
    private val service: OppgaveService
) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-oppgave-thread"
        log.info("Starting new thread:$name to process oppgaver")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                service.process()
            } catch (exception: Throwable) {
                log.error("Exception caught while processing, exception:$exception")
                Thread.sleep(1000)
            }
        }
    }
}
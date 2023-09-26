package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MicrometerMetrics
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class OppgaveProcessingThread(
    private val service: OppgaveService,
    private val unleash: Unleash,
    private val metrics: MicrometerMetrics,
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
                if(unleash.isEnabled(NavUnleashConfig.Feature.OPPRETT_OPPGAVER.toggleName)){
                    metrics.oppgaverProsessertTidsbruk.recordCallable{  service.process() }
                }
            } catch (exception: Throwable) {
                metrics.oppgaverFeiletTidsbruk.recordCallable {
                    log.warn("Exception caught while processing, exception:$exception")
                    Thread.sleep(1000)
                }
                metrics.oppgaverFeiletTidsbruk.totalTime(TimeUnit.MILLISECONDS)
            }
        }
    }
}
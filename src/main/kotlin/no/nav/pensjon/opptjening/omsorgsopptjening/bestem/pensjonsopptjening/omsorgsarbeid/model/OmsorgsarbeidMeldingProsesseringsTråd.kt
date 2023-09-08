package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class OmsorgsarbeidMeldingProsesseringsTråd(
    private val handler: OmsorgsarbeidMeldingService,
    private val unleash: Unleash
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
                if(unleash.isEnabled(NavUnleashConfig.Feature.BEHANDLING.toggleName)) {
                    handler.process()
                }
            } catch (exception: Throwable) {
                log.warn("Exception caught while processing, exception:$exception")
                Thread.sleep(1000)
            }
        }
    }
}
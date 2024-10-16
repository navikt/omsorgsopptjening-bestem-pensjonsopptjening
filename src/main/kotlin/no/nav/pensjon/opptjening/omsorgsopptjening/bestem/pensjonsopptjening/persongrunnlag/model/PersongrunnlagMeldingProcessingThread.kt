package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

class PersongrunnlagMeldingProcessingThread(
    private val service: PersongrunnlagMeldingProcessingService,
    private val unleash: Unleash,
    private val omsorgsarbeidMetricsMåling: OmsorgsarbeidProcessingMetrikker,
    private val omsorgsarbeidMetricsFeilmåling: OmsorgsarbeidProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }

    @PostConstruct
    fun init() {
        val name = "prosesser-persongrunnlag-melding-thread"
        log.info("Starting new thread:$name to process persongrunnlag")
        Thread(this, name).start()
    }

    override fun run() {
        while (true) {
            try {
                if (unleash.isEnabled(NavUnleashConfig.Feature.BEHANDLING.toggleName) && datasourceReadinessCheck.isReady()) {
                    omsorgsarbeidMetricsMåling.oppdater {
                        service.process()?.let { null } ?: run {
                            Thread.sleep(1000)
                            null
                        }
                    }
                }
            } catch (exception: Throwable) {
                omsorgsarbeidMetricsFeilmåling.oppdater {
                    log.warn("Exception caught while processing ${exception::class.qualifiedName}")
                }
            }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import io.getunleash.Unleash
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class OppgaveProcessingThread(
    private val service: OppgaveService,
    private val unleash: Unleash,
    private val oppgaveProcessingMetricsMåling: OppgaveProcessingMetrikker,
    private val oppgaveProcessingMetricsFeilmåling: OppgaveProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
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
                if (unleash.isEnabled(NavUnleashConfig.Feature.OPPRETT_OPPGAVER.toggleName) && datasourceReadinessCheck.isReady()) {
                    oppgaveProcessingMetricsMåling.oppdater {
                        service.process()
                    }
                }
            } catch (exception: Throwable) {
                oppgaveProcessingMetricsFeilmåling.oppdater {
                    log.warn("Exception caught while processing ${exception::class.qualifiedName}",exception)
                }
            }
        }
    }
}
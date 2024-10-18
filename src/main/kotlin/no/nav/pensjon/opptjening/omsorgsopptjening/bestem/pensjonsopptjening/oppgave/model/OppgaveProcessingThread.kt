package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import org.slf4j.LoggerFactory


class OppgaveProcessingThread(
    private val service: OppgaveService,
    private val unleash: Unleash,
    private val oppgaveProcessingMetricsMåling: OppgaveProcessingMetrikker,
    private val oppgaveProcessingMetricsFeilmåling: OppgaveProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }

    init {
        log.info("Starting new thread to process oppgaver")
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
                    log.warn("Exception caught while processing ${exception::class.qualifiedName}")
                }
            }
        }
    }
}
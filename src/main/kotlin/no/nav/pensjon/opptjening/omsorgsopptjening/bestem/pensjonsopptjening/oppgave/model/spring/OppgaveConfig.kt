package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OppgaveConfig {

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun oppgaveProcessingThread(
        service: OppgaveService,
        unleash: UnleashWrapper,
        oppgaveProcessingMetricsMåling: OppgaveProcessingMetrikker,
        oppgaveProcessingMetricsFeilmåling: OppgaveProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): OppgaveProcessingTask {
        return OppgaveProcessingTask(
            service = service,
            unleash = unleash,
            oppgaveProcessingMetricsMåling = oppgaveProcessingMetricsMåling,
            oppgaveProcessingMetricsFeilmåling = oppgaveProcessingMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck,
        )
    }
}
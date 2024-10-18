package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.spring

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class OppgaveConfig {

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun oppgaveProcessingThread(
        service: OppgaveService,
        unleash: Unleash,
        oppgaveProcessingMetricsMåling: OppgaveProcessingMetrikker,
        oppgaveProcessingMetricsFeilmåling: OppgaveProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): OppgaveProcessingThread {
        return OppgaveProcessingThread(
            service = service,
            unleash = unleash,
            oppgaveProcessingMetricsMåling = oppgaveProcessingMetricsMåling,
            oppgaveProcessingMetricsFeilmåling = oppgaveProcessingMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck
        )
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.spring

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningProcessingServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningServiceImpl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class GodskrivOpptjeningConfig {

    @Bean
    fun godskrivOpptjeningService(
        @Qualifier("godskrivOpptjening") godskrivClient: GodskrivOpptjeningClient,
        godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
        behandlingRepo: BehandlingRepo,
        oppgaveService: OppgaveService,
    ): GodskrivOpptjeningService {
        return GodskrivOpptjeningServiceImpl(
            godskrivClient = godskrivClient,
            godskrivOpptjeningRepo = godskrivOpptjeningRepo,
            behandlingRepo = behandlingRepo,
            oppgaveService = oppgaveService,
        )
    }

    @Bean
    fun godskrivOpptjeningProcessingService(
        godskrivOpptjeningService: GodskrivOpptjeningService,
        transactionTemplate: NewTransactionTemplate,
    ): GodskrivOpptjeningProcessingService {
        return GodskrivOpptjeningProcessingServiceImpl(
            godskrivOpptjeningService = godskrivOpptjeningService,
            transactionTemplate = transactionTemplate,
        )
    }

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    internal fun godskrivOpptjeningProcessingThread(
        service: GodskrivOpptjeningProcessingService,
        unleash: UnleashWrapper,
        godskrivProcessingMetricsMåling: GodskrivProcessingMetrikker,
        godskrivProcessingMetricsFeilmåling: GodskrivProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): GodskrivOpptjeningProcessingTask {
        return GodskrivOpptjeningProcessingTask(
            service = service,
            unleash = unleash,
            godskrivProcessingMetricsMåling = godskrivProcessingMetricsMåling,
            godskrivProcessingMetricsFeilmåling = godskrivProcessingMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck
        )
    }

    @Bean
    fun godskrivProcessingMetrikker(
        meterRegistry: MeterRegistry
    ): GodskrivProcessingMetrikker {
        return GodskrivProcessingMetrikker(meterRegistry)
    }

    @Bean
    fun godskrivProcessingMetrikkerFeilmåling(
        meterRegistry: MeterRegistry
    ): GodskrivProcessingMetricsFeilmåling {
        return GodskrivProcessingMetricsFeilmåling(meterRegistry)
    }
}
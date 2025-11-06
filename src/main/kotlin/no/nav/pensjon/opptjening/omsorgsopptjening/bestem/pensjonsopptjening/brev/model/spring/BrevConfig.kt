package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.spring

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevProcessingTask
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.TimeLock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class BrevConfig {

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun brevProcessingThread(
        service: BrevService,
        unleash: UnleashWrapper,
        brevProcessingMetricsMåling: BrevProcessingMetrikker,
        brevProcessingMetricsFeilmåling: BrevProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
        timeLockProperties: TimeLock.Properties,
    ): BrevProcessingTask {
        return BrevProcessingTask(
            service = service,
            unleash = unleash,
            brevProcessingMetricsMåling = brevProcessingMetricsMåling,
            brevProcessingMetricsFeilmåling = brevProcessingMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck,
            timeLockProperties = timeLockProperties,
        )
    }
}
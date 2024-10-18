package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.spring

import io.getunleash.Unleash
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevProcessingThread
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class BrevConfig {

    @Bean
    @Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
    fun brevProcessingThread(
        service: BrevService,
        unleash: Unleash,
        brevProcessingMetricsMåling: BrevProcessingMetrikker,
        brevProcessingMetricsFeilmåling: BrevProcessingMetricsFeilmåling,
        datasourceReadinessCheck: DatasourceReadinessCheck,
    ): BrevProcessingThread {
        return BrevProcessingThread(
            service = service,
            unleash = unleash,
            brevProcessingMetricsMåling = brevProcessingMetricsMåling,
            brevProcessingMetricsFeilmåling = brevProcessingMetricsFeilmåling,
            datasourceReadinessCheck = datasourceReadinessCheck
        )
    }
}
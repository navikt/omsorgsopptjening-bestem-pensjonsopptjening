package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.spring

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetrikker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OmsorgsarbeidMetricsConfig {

    @Bean
    fun omsorgsarbeidProcessingMetrics(meterRegistry: MeterRegistry): OmsorgsarbeidProcessingMetrikker {
        return OmsorgsarbeidProcessingMetrikker(meterRegistry)
    }

    @Bean
    fun omsorgsarbedProcessingMetricsFeilmåling(meterRegistry: MeterRegistry): OmsorgsarbeidProcessingMetricsFeilmåling {
        return OmsorgsarbeidProcessingMetricsFeilmåling(meterRegistry)
    }
}
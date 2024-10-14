package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.spring

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OmsorgsarbeidMetricsConfig {

    @Bean
    fun omsorgsarbeidProcessingMetrics(meterRegistry: MeterRegistry): Metrikker<List<FullførteBehandlinger?>?> {
        return OmsorgsarbeidProcessingMetrikker(meterRegistry)
    }

    @Bean
    fun omsorgsarbedProcessingMetricsFeilmåling(meterRegistry: MeterRegistry): Metrikker<Unit> {
        return OmsorgsarbeidProcessingMetricsFeilmåling(meterRegistry)
    }
}
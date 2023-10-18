package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsFeilmåling
import org.springframework.stereotype.Component

@Component
class OmsorgsarbeidProcessingMetricsFeilmåling(registry: MeterRegistry): MetricsFeilmåling<Unit> {

    private val omsorgsarbeidFeiletTidsbruk = registry.timer("prosesseringTidsbruk","omsorgsarbeidFeilet", "tidsbruk")
    private val antallFeiledeOmsorgsarbeid = registry.counter("prosesseringsenhetFeilede","antall", "omsorgsarbeid")


    override fun målfeil(lambda: () -> Unit) {
        omsorgsarbeidFeiletTidsbruk.recordCallable(lambda)
        antallFeiledeOmsorgsarbeid.increment()
    }
}
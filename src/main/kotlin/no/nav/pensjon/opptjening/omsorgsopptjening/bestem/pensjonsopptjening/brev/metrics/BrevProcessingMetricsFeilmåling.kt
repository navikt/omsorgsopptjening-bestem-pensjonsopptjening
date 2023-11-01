package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsFeilmåling
import org.springframework.stereotype.Component

@Component
class BrevProcessingMetricsFeilmåling(val registry: MeterRegistry): MetricsFeilmåling<Unit> {

    private val brevFeiletTidsbruk = registry.timer("prosessering", "tidsbruk", "brevFeilet")
    private val antallFeiledeBrev = registry.counter("prosesseringsenhetFeilede","antall", "brev")

    override fun målfeil(lambda: () -> Unit) {
        antallFeiledeBrev.increment()
        brevFeiletTidsbruk.recordCallable(lambda)
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsFeilmåling
import org.springframework.stereotype.Component

@Component
class GodskrivProcessingMetricsFeilmåling(registry: MeterRegistry): MetricsFeilmåling<Unit>  {

    private val godskrivFeiletTidsbruk = registry.timer("prosessering","tidsbruk", "godskrivFeilet")
    private val antallFeiledeGodskriving = registry.counter("prosesseringsenhetFeilede","antall", "godskriving")
    override fun målfeil(lambda: () -> Unit) {
        antallFeiledeGodskriving.increment()
        godskrivFeiletTidsbruk.recordCallable(lambda)
    }
}
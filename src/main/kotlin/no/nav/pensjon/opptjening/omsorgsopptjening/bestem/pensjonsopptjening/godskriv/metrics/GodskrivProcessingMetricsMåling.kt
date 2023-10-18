package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsMåling
import org.springframework.stereotype.Component

@Component
class GodskrivProcessingMetricsMåling(registry: MeterRegistry):
    MetricsMåling<GodskrivOpptjening.Persistent?> {

    private val godskrivProsessertTidsbruk = registry.timer("prosesseringTidsbruk","godskrivProsessert", "tidsbruk")

    override fun mål(lambda: () -> GodskrivOpptjening.Persistent?): GodskrivOpptjening.Persistent? {
        return godskrivProsessertTidsbruk.recordCallable(lambda)
    }
}
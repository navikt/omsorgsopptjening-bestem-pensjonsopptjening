package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsMåling
import org.springframework.stereotype.Component

@Component
class BrevProcessingMetricsMåling(registry: MeterRegistry): MetricsMåling<Brev?> {

    private val brevProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "brevProsessert")

    override fun mål(lambda: () -> Brev?): Brev? {
        return brevProsessertTidsbruk.recordCallable(lambda)
    }
}
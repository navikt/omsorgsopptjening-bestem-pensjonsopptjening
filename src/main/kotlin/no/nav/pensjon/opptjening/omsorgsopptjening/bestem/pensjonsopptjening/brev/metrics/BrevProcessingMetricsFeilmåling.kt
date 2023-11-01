package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import org.springframework.stereotype.Component

@Component
class BrevProcessingMetricsFeilmåling(registry: MeterRegistry) : Metrikker<Unit> {

    private val brevFeiletTidsbruk = registry.timer("prosessering", "tidsbruk", "brevFeilet")
    private val antallFeiledeBrev = registry.counter("prosesseringsenhetFeilede", "antall", "brev")
    override fun oppdater(lambda: () -> Unit) {
        brevFeiletTidsbruk.recordCallable(lambda)
        antallFeiledeBrev.increment()
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import org.springframework.stereotype.Component

@Component
class GodskrivProcessingMetricsFeilmåling(registry: MeterRegistry) : Metrikker<Unit> {

    private val godskrivFeiletTidsbruk = registry.timer("prosessering", "tidsbruk", "godskrivFeilet")
    private val antallFeiledeGodskriving = registry.counter("prosesseringsenhetFeilede", "antall", "godskriving")
    override fun oppdater(lambda: () -> Unit) {
        godskrivFeiletTidsbruk.recordCallable(lambda)
        antallFeiledeGodskriving.increment()
    }
}
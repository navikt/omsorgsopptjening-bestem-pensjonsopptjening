package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker

class OmsorgsarbeidProcessingMetricsFeilmåling(registry: MeterRegistry) : Metrikker<Unit> {

    private val omsorgsarbeidFeiletTidsbruk = registry.timer("prosessering", "tidsbruk", "omsorgsarbeidFeilet")
    private val antallFeiledeOmsorgsarbeid = registry.counter("prosesseringsenhetFeilede", "antall", "omsorgsarbeid")

    override fun oppdater(lambda: () -> Unit) {
        omsorgsarbeidFeiletTidsbruk.recordCallable(lambda)
        antallFeiledeOmsorgsarbeid.increment()
    }
}
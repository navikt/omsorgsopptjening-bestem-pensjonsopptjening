package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class MicrometerMetrics(private val registry: MeterRegistry) {

    val oppgaverProsessertTidsbruk = registry.timer("prosesseringTidsbruk","oppgaverProsessert", "tidsbruk")
    val oppgaverFeiletTidsbruk = registry.timer("prosesseringTidsbruk","oppgaverFeilet", "tidsbruk")
    val omsorgsarbeidProsessertTidsbruk = registry.timer("prosesseringTidsbruk","omsorgsarbeidProsessert", "tidsbruk")
    val omsorgsarbeidFeiletTidsbruk = registry.timer("prosesseringTidsbruk","omsorgsarbeidFeilet", "tidsbruk")
    val godskrivProsessertTidsbruk = registry.timer("prosesseringTidsbruk","godskrivProsessert", "tidsbruk")
    val godskrivFeiletTidsbruk = registry.timer("prosesseringTidsbruk","godskrivFeilet", "tidsbruk")
    val brevProsessertTidsbruk = registry.timer("prosesseringTidsbruk","brevProsessert", "tidsbruk")
    val brevFeiletTidsbruk = registry.timer("prosesseringTidsbruk","brevFeilet", "tidsbruk")
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker

class GodskrivProcessingMetrikker(registry: MeterRegistry) : Metrikker<List<GodskrivOpptjening.Persistent>?> {

    private val godskrivProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "godskrivProsessert")
    private val antallGodskrevet = registry.counter("godskriving", "antall", "opprettet")

    override fun oppdater(lambda: () -> List<GodskrivOpptjening.Persistent>?): List<GodskrivOpptjening.Persistent>? {
        return godskrivProsessertTidsbruk.recordCallable(lambda)?.also {
            antallGodskrevet.increment()
        }
    }
}
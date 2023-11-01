package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import org.springframework.stereotype.Component

@Component
class GodskrivProcessingMetrikker(registry: MeterRegistry) : Metrikker<GodskrivOpptjening.Persistent?> {

    private val godskrivProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "godskrivProsessert")
    private val antallGodskrevet = registry.counter("godskriving", "antall", "opprettet")

    override fun oppdater(lambda: () -> GodskrivOpptjening.Persistent?): GodskrivOpptjening.Persistent? {
        return godskrivProsessertTidsbruk.recordCallable(lambda)?.also {
            antallGodskrevet.increment()
        }
    }
}
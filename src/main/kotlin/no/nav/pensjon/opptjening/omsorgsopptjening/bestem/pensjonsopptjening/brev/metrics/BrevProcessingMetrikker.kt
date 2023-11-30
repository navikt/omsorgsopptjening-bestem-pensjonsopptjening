package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import org.springframework.stereotype.Component

@Component
class BrevProcessingMetrikker(registry: MeterRegistry) : Metrikker<List<Brev>?> {

    private val brevProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "brevProsessert")

    override fun oppdater(lambda: () -> List<Brev>?): List<Brev>? {
        return brevProsessertTidsbruk.recordCallable(lambda)?.also {
            //TODO
        }
    }
}
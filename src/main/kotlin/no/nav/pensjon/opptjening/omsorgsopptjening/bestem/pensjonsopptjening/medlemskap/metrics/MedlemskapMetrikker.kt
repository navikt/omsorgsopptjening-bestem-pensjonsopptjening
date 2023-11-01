package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskap
import org.springframework.stereotype.Component

@Component
class MedlemskapMetrikker(registry: MeterRegistry) : Metrikker<Medlemskap?> {

    private val oppslag = registry.counter("medlemskap", "antall", "oppslag")
    private val ikkeFunnet = registry.counter("medlemskap", "antall", "ikke_funnet")
    private val medlem = registry.counter("medlemskap", "antall", "ja")
    private val ikkeMedlem = registry.counter("medlemskap", "antall", "nei")
    private val ukjent = registry.counter("medlemskap", "antall", "ukjent")

    override fun oppdater(lambda: () -> Medlemskap?): Medlemskap? {
        oppslag.increment()
        return lambda().also {
            when (it) {
                is Medlemskap.Ja -> medlem.increment()
                is Medlemskap.Nei -> ikkeMedlem.increment()
                is Medlemskap.Ukjent -> ukjent.increment()
                null -> {
                    ukjent.increment()
                    ikkeFunnet.increment()
                }
            }
        }
    }
}
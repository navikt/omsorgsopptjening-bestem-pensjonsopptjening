package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import org.springframework.stereotype.Component

@Component
class PENBrevMetrikker(registry: MeterRegistry) : Metrikker<Journalpost> {
    private val antallSendteBrev = registry.counter("brev", "antall", "opprettet")
    override fun oppdater(lambda: () -> Journalpost): Journalpost {
        return lambda().also {
            antallSendteBrev.increment()
        }
    }
}
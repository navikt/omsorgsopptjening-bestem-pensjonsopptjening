package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsMåling
import org.springframework.stereotype.Component

@Component
class PENBrevMetricsMåling(registry: MeterRegistry): MetricsMåling<Journalpost> {
    private val antallSendteBrev = registry.counter("brev", "antall", "opprettet")
    override fun mål(lambda: () -> Journalpost): Journalpost {
        antallSendteBrev.increment()
        return lambda.invoke()
    }
}
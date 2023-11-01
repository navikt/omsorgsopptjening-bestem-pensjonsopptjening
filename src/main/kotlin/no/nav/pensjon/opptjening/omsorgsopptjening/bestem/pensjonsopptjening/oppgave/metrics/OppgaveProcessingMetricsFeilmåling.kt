package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsFeilmåling
import org.springframework.stereotype.Component

@Component
class OppgaveProcessingMetricsFeilmåling(registry: MeterRegistry): MetricsFeilmåling<Unit> {

    private val antallFeiledeOppgaver = registry.counter("prosesseringsenhetFeilede", "antall", "oppgaver")
    private val oppgaverFeiletTidsbruk = registry.timer("prosessering","tidsbruk", "oppgaverFeilet")
    override fun målfeil(lambda: () -> Unit) {
        oppgaverFeiletTidsbruk.recordCallable(lambda)
        antallFeiledeOppgaver.increment()
    }
}
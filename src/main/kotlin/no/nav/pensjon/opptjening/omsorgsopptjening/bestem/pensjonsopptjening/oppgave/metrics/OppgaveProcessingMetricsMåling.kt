package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.MetricsMåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import org.springframework.stereotype.Component

@Component
class OppgaveProcessingMetricsMåling(registry: MeterRegistry): MetricsMåling<Oppgave?> {

    private val oppgaverProsessertTidsbruk = registry.timer("prosesseringTidsbruk","oppgaverProsessert", "tidsbruk")
    private val antallOpprettedeOppgaver = registry.counter("oppgaver", "antall", "opprettet")

    override fun mål(lambda: () -> Oppgave?): Oppgave? {
        val oppgave = oppgaverProsessertTidsbruk.recordCallable(lambda)
        antallOpprettedeOppgaver.increment()
        return oppgave
    }
}
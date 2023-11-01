package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.Metrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import org.springframework.stereotype.Component

@Component
class OppgaveProcessingMetrikker(registry: MeterRegistry) : Metrikker<Oppgave?> {

    private val oppgaverProsessertTidsbruk = registry.timer("prosessering", "tidsbruk", "oppgaverProsessert")
    private val antallOpprettedeOppgaver = registry.counter("oppgaver", "antall", "opprettet")

    override fun oppdater(lambda: () -> Oppgave?): Oppgave? {
        return oppgaverProsessertTidsbruk.recordCallable(lambda)?.also {
            antallOpprettedeOppgaver.increment()
        }
    }
}
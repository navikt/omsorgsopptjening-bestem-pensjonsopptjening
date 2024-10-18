package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import java.time.Instant
import java.util.UUID

data class Kontrollbehandling(
    val kontrollId: UUID,
    val opprettet: Instant,
    val kontrollmeldingId: UUID,
    val statushistorikk: List<KontrollbehandlingStatus> = listOf(KontrollbehandlingStatus.Klar()),
    val referanse: String,
    val omsorgsÅr: Int,
    val kafkameldingid: UUID,
) {
    val status: KontrollbehandlingStatus get() = statushistorikk.last()

    fun ferdig(): Kontrollbehandling {
        return copy(statushistorikk = statushistorikk + status.ferdig())
    }

    fun retry(melding: String): Kontrollbehandling {
        return copy(statushistorikk = statushistorikk + status.retry(melding))
    }
}
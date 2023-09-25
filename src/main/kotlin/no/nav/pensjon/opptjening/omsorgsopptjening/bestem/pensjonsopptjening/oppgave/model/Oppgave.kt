package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class Oppgave {
    abstract val id: UUID?
    abstract val opprettet: Instant?
    abstract val detaljer: OppgaveDetaljer
    abstract val behandlingId: UUID?
    abstract val meldingId: UUID
    abstract val correlationId: CorrelationId
    abstract val statushistorikk: List<Status>
    abstract val innlesingId: InnlesingId

    val status get() = statushistorikk.last()
    val mottaker get() = detaljer.mottaker()
    val oppgavetekst get() = detaljer.oppgavetekst()

    data class Transient(
        override val detaljer: OppgaveDetaljer,
        override val behandlingId: UUID?,
        override val meldingId: UUID,
        override val correlationId: CorrelationId,
        override val innlesingId: InnlesingId
    ) : Oppgave() {
        override val id: UUID? = null
        override val opprettet: Instant? = null
        override val statushistorikk: List<Status> = listOf(Status.Klar())
    }

    data class Persistent(
        override val id: UUID,
        override val opprettet: Instant,
        override val detaljer: OppgaveDetaljer,
        override val behandlingId: UUID?, //kan være vi feiler før vi får behandlet
        override val meldingId: UUID,
        override val correlationId: CorrelationId,
        override val statushistorikk: List<Status> = listOf(Status.Klar()),
        override val innlesingId: InnlesingId
    ) : Oppgave() {
        fun ferdig(oppgaveId: String): Persistent {
            return copy(statushistorikk = statushistorikk + status.ferdig(oppgaveId))
        }

        fun retry(melding: String): Persistent {
            return copy(statushistorikk = statushistorikk + status.retry(melding))
        }
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
    )
    sealed class Status {

        open fun ferdig(oppgaveId: String): Ferdig {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Ferdig")
        }

        open fun retry(melding: String): Status {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Retry")
        }

        @JsonTypeName("Klar")
        data class Klar(
            val tidspunkt: Instant = Instant.now()
        ) : Status() {
            override fun ferdig(oppgaveId: String): Ferdig {
                return Ferdig(oppgaveId = oppgaveId)
            }

            override fun retry(melding: String): Status {
                return Retry(melding = melding)
            }
        }

        @JsonTypeName("Ferdig")
        data class Ferdig(
            val tidspunkt: Instant = Instant.now(),
            val oppgaveId: String,
        ) : Status()

        @JsonTypeName("Retry")
        data class Retry(
            val tidspunkt: Instant = Instant.now(),
            val antallForsøk: Int = 1,
            val maxAntallForsøk: Int = 3,
            val karanteneTil: Instant = tidspunkt.plus(5, ChronoUnit.HOURS),
            val melding: String,
        ) : Status() {
            override fun ferdig(oppgaveId: String): Ferdig {
                return Ferdig(oppgaveId = oppgaveId)
            }

            override fun retry(melding: String): Status {
                return when {
                    antallForsøk < maxAntallForsøk -> {
                        Retry(
                            tidspunkt = Instant.now(),
                            antallForsøk = antallForsøk + 1,
                            melding = melding,
                        )
                    }

                    antallForsøk == maxAntallForsøk -> {
                        Feilet()
                    }

                    else -> {
                        super.retry(melding)
                    }
                }
            }
        }

        @JsonTypeName("Feilet")
        data class Feilet(
            val tidspunkt: Instant = Instant.now(),
        ) : Status()
    }
}
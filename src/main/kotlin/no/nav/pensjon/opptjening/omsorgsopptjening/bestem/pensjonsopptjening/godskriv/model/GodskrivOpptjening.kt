package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class GodskrivOpptjening {
    abstract val id: UUID?
    abstract val opprettet: Instant?
    abstract val behandlingId: UUID
    abstract val meldingId: UUID
    abstract val correlationId: CorrelationId
    abstract val statushistorikk: List<Status>
    abstract val omsorgsyter: String?
    abstract val innlesingId: InnlesingId

    companion object {
        const val OMSORGSPOENG_GODSKRIVES = 3.5
    }
    data class Transient(
        override val behandlingId: UUID,
        override val meldingId: UUID,
        override val correlationId: CorrelationId,
        override val innlesingId: InnlesingId
    ): GodskrivOpptjening(){
        override val id: UUID? = null
        override val opprettet: Instant? = null
        override val omsorgsyter: String? = null
        override val statushistorikk: List<Status> = listOf(Status.Klar())
        val status get() =  statushistorikk.last()
    }

    data class Persistent(
        override val id: UUID,
        override val opprettet: Instant,
        override val behandlingId: UUID,
        override val meldingId: UUID,
        override val correlationId: CorrelationId,
        override val omsorgsyter: String,
        override val innlesingId: InnlesingId,
        override val statushistorikk: List<Status> = listOf(Status.Klar()),
    ): GodskrivOpptjening(){
        val status get() =  statushistorikk.last()

        fun ferdig(): Persistent {
            return copy(statushistorikk = statushistorikk + status.ferdig())
        }

        fun retry(melding: String): Persistent {
            return copy(statushistorikk = statushistorikk + status.retry(melding))
        }

        fun opprettOppgave(): Oppgave.Transient? {
            return if (status is Status.Feilet) {
                Oppgave.Transient(
                    detaljer = OppgaveDetaljer.UspesifisertFeilsituasjon(
                        omsorgsyter = omsorgsyter,
                    ),
                    behandlingId = behandlingId,
                    meldingId = meldingId,
                    correlationId = correlationId,
                    innlesingId = innlesingId,
                )
            } else {
                null
            }
        }
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
    )
    sealed class Status {

        open fun ferdig(): Ferdig {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Ferdig")
        }

        open fun retry(melding: String): Status {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Retry")
        }

        @JsonTypeName("Klar")
        data class Klar(
            val tidspunkt: Instant = Instant.now()
        ) : Status() {
            override fun ferdig(): Ferdig {
                return Ferdig()
            }

            override fun retry(melding: String): Status {
                return Retry(melding = melding)
            }
        }

        @JsonTypeName("Ferdig")
        data class Ferdig(
            val tidspunkt: Instant = Instant.now(),
        ) : Status()

        @JsonTypeName("Retry")
        data class Retry(
            val tidspunkt: Instant = Instant.now(),
            val antallForsøk: Int = 1,
            val maxAntallForsøk: Int = 3,
            val karanteneTil: Instant = tidspunkt.plus(5, ChronoUnit.HOURS),
            val melding: String,
        ) : Status() {
            override fun ferdig(): Ferdig {
                return Ferdig()
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
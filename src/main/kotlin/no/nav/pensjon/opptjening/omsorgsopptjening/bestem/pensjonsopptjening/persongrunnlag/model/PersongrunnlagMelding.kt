package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class PersongrunnlagMelding {
    abstract val id: UUID?
    abstract val opprettet: Instant
    abstract val innhold: PersongrunnlagMeldingKafka
    abstract val statushistorikk: List<Status>

    val correlationId get() = innhold.correlationId
    val innlesingId get() = innhold.innlesingId
    val status: Status get() = statushistorikk.last()

    data class Lest(
        override val innhold: PersongrunnlagMeldingKafka,
        override val opprettet: Instant = now()
    ) : PersongrunnlagMelding() {
        override val id: UUID? = null
        override val statushistorikk: List<Status> = listOf(Status.Klar())
    }

    data class Mottatt(
        override val id: UUID,
        override val opprettet: Instant,
        override val innhold: PersongrunnlagMeldingKafka,
        override val statushistorikk: List<Status> = listOf(Status.Klar()),
    ) : PersongrunnlagMelding() {
        fun ferdig(): Mottatt {
            return copy(statushistorikk = statushistorikk + status.ferdig())
        }

        fun retry(melding: String): Mottatt {
            return copy(statushistorikk = statushistorikk + status.retry(melding))
        }

        fun opprettOppgave(): Oppgave.Transient? {
            return if (status is Status.Feilet) {
                Oppgave.Transient(
                    detaljer = OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = innhold.omsorgsyter,
                        oppgavetekst = """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."""
                    ),
                    behandlingId = null,
                    meldingId = id,
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
            val tidspunkt: Instant = now()
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


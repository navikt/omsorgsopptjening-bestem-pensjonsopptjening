package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import java.util.*
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

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
        override val opprettet: Instant = now(),
        val kopiertFra: Mottatt? = null,
    ) : PersongrunnlagMelding() {
        override val statushistorikk: List<Status> =
            when (kopiertFra) {
                null -> listOf(Status.Klar())
                else -> listOf(Status.Kopiert(kopiertFra.id), Status.Klar())
            }
        override val id: UUID? = null
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

        fun avsluttet(melding: String): Mottatt {
            return copy(statushistorikk = statushistorikk + status.avsluttet(melding))
        }

        fun stoppet(begrunnelse: String? = null): Mottatt {
            return copy(statushistorikk = statushistorikk + status.stoppet(begrunnelse))
        }

        fun opprettOppgave(): Oppgave.Transient? {
            return if (status is Status.Feilet) {
                Oppgave.Transient(
                    detaljer = OppgaveDetaljer.MottakerOgTekst(
                        oppgavemottaker = innhold.omsorgsyter,
                        oppgavetekst = setOf(Oppgave.kunneIkkeBehandlesAutomatisk())
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

        open fun avsluttet(melding: String): Avsluttet {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Avsluttet")
        }

        open fun stoppet(begrunnelse: String?): Stoppet {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Stoppet")
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

            override fun avsluttet(melding: String): Avsluttet {
                return Avsluttet(melding = melding)
            }

            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }
        }

        @JsonTypeName("Ferdig")
        data class Ferdig(
            val tidspunkt: Instant = now(),

            ) : Status() {
            override fun avsluttet(melding: String): Avsluttet {
                return Avsluttet(melding = melding)
            }

            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }
        }

        @JsonTypeName("Avsluttet")
        data class Avsluttet(
            val tidspunkt: Instant = now(),
            val melding: String
        ) : Status() {
            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }
        }

        @JsonTypeName("Stoppet")
        data class Stoppet(
            val tidspunkt: Instant = now(),
            val begrunnelse: String? = null,
        ) : Status() {
            override fun avsluttet(melding: String): Avsluttet {
                return Avsluttet(melding = melding)
            }
        }

        @JsonTypeName("Retry")
        data class Retry(
            val tidspunkt: Instant = now(),
            val antallForsøk: Int = 1,
            val maxAntallForsøk: Int = 3,
            val karanteneTil: Instant = tidspunkt.plus(5, ChronoUnit.HOURS),
            val melding: String,
        ) : Status() {
            override fun ferdig(): Ferdig {
                return Ferdig()
            }

            override fun avsluttet(melding: String): Avsluttet {
                return Avsluttet(melding = melding)
            }

            override fun retry(melding: String): Status {
                return when {
                    antallForsøk < maxAntallForsøk -> {
                        Retry(
                            tidspunkt = now(),
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

            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }

        }

        // TODO: riktig måte å håndtere dette på? Inngår ikke i mormal flyt, er bare her for å ta vare på info
        data class Kopiert(
            val kopiertFra: UUID
        ) : Status()

        @JsonTypeName("Feilet")
        data class Feilet(
            val tidspunkt: Instant = now(),
        ) : Status() {
            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }

            override fun avsluttet(melding: String): Avsluttet {
                return Avsluttet(melding = melding)
            }
        }
    }
}
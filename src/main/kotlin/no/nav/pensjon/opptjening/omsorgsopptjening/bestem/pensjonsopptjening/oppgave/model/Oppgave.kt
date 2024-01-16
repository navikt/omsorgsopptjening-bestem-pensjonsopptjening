package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave.KanselleringsResultat.*
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

sealed class Oppgave {
    abstract val detaljer: OppgaveDetaljer
    abstract val behandlingId: UUID? //kan være null dersom alt feiler fra start
    abstract val meldingId: UUID
    abstract val statushistorikk: List<Status>

    val status: Status get() = statushistorikk.last()
    val mottaker: String get() = detaljer.mottaker()
    val oppgavetekst: Set<String> get() = detaljer.oppgavetekst()

    companion object Oppgavetekster {
        fun flereOmsorgsytereMedLikeMyeOmsorgFødselsår(omsorgsmottaker: String): String =
            """Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr $omsorgsmottaker i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."""

        fun flereOmsorgsytereMedLikeMyeOmsorg(omsorgsmottaker: String, annenOmsorgsyter: String): String =
            """Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr $omsorgsmottaker. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr $annenOmsorgsyter mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene."""

        fun kunneIkkeBehandlesAutomatisk(): String =
            """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."""
    }

    /**
     * Knytter oppgaven og [detaljer] til aktuell [behandlingId] og/eller [meldingId].
     */
    data class Transient(
        override val detaljer: OppgaveDetaljer,
        override val behandlingId: UUID?,
        override val meldingId: UUID,
    ) : Oppgave() {
        override val statushistorikk: List<Status> = listOf(Status.Klar())
    }

    data class Persistent(
        val id: UUID,
        val opprettet: Instant,
        val correlationId: CorrelationId,
        val innlesingId: InnlesingId,
        override val detaljer: OppgaveDetaljer,
        override val behandlingId: UUID?, //kan være vi feiler før vi får behandlet
        override val meldingId: UUID,
        override val statushistorikk: List<Status> = listOf(Status.Klar()),
    ) : Oppgave() {
        fun ferdig(oppgaveId: String): Persistent {
            return copy(statushistorikk = statushistorikk + status.ferdig(oppgaveId))
        }

        fun stoppet(): Persistent {
            return copy(statushistorikk = statushistorikk + status.stoppet())
        }

        fun retry(melding: String): Persistent {
            return copy(statushistorikk = statushistorikk + status.retry(melding))
        }

        fun restart(): Persistent {
            return copy(statushistorikk = statushistorikk + status.klar())
        }

        fun kansellert(begrunnelse: String, kanselleringsResultat: KanselleringsResultat): Persistent {
            return copy(statushistorikk = statushistorikk + status.kansellert(begrunnelse, kanselleringsResultat))
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

        open fun stoppet(): Status {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Retry")
        }

        open fun klar(): Status {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Klar")
        }

        open fun kansellert(begrunnelse: String, kanselleringsResultat: KanselleringsResultat): Status {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Kansellert")
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

            override fun stoppet(): Stoppet {
                return Stoppet()
            }

            override fun klar(): Status {
                return Klar()
            }

        }

        @JsonTypeName("Ferdig")
        data class Ferdig(
            val tidspunkt: Instant = Instant.now(),
            val oppgaveId: String,
        ) : Status() {
            override fun stoppet(): Stoppet {
                return Stoppet()
            }

            override fun kansellert(begrunnelse: String, kanselleringsResultat: KanselleringsResultat): Status {
                return Kansellert(begrunnelse = begrunnelse, kanselleringsResultat = kanselleringsResultat)
            }
        }

        @JsonTypeName("Stoppet")
        data class Stoppet(
            val tidspunkt: Instant = Instant.now(),
            val begrunnelse: String? = null,
        ) : Status()

        @JsonTypeName("Kansellert")
        data class Kansellert(
            val tidspunkt: Instant = Instant.now(),
            val begrunnelse: String,
            val kanselleringsResultat: KanselleringsResultat,
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

            override fun stoppet(): Stoppet {
                return Stoppet()
            }

            override fun klar(): Status {
                return Klar()
            }

        }

        @JsonTypeName("Feilet")
        data class Feilet(
            val tidspunkt: Instant = Instant.now(),
        ) : Status() {
            override fun stoppet(): Stoppet {
                return Stoppet()
            }

            override fun klar(): Status {
                return Klar()
            }
        }
    }

    enum class KanselleringsResultat {
        OPPGAVEN_ER_KANSELLERT,
        OPPGAVEN_VAR_ALLEREDE_KANSELLERT,
        FANT_IKKE_OPPGAVEN_I_OMSORGSOPPTJENING,
        FANT_IKKE_OPPGAVEN,
        OPPGAVEN_ER_FERDIGBEHANDLET,
        OPPDATERING_FEILET
    }
}
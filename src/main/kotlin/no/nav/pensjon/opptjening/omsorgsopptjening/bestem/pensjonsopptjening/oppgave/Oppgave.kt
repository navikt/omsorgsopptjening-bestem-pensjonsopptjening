package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class OppgaveDetaljer {
    data class FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
    ) : OppgaveDetaljer() {
        val oppgaveTekst: String =
            """Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr $omsorgsmottaker i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."""
    }

    data class FlereOmsorgytereMedLikeMyeOmsorg(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
        val annenOmsorgsyter: String,
    ) : OppgaveDetaljer() {
        val oppgaveTekst: String =
            """Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr $omsorgsmottaker. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr $annenOmsorgsyter mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene."""
    }

    data class UspesifisertFeilsituasjon(
        val omsorgsyter: String,
    ) : OppgaveDetaljer() {
        val oppgaveTekst: String =
            """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."""
    }
}

data class Oppgave(
    val id: UUID? = null,
    val opprettet: Instant? = null,
    val detaljer: OppgaveDetaljer,
    val behandlingId: UUID?,
    val meldingId: UUID,
    val correlationId: UUID? = null,
    val statushistorikk: List<Status> = listOf(Status.Klar()),
) {
    val status = statushistorikk.last()

    fun ferdig(oppgaveId: String): Oppgave {
        return copy(statushistorikk = statushistorikk + status.ferdig(oppgaveId))
    }

    fun retry(melding: String): Oppgave {
        return copy(statushistorikk = statushistorikk + status.retry(melding))
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
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

    abstract fun mottaker(): String
    abstract fun oppgavetekst(): String

    @JsonTypeName("FlereOmsorgytereMedLikeMyeOmsorgIFødselsår")
    data class FlereOmsorgytereMedLikeMyeOmsorgIFødselsår(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
    ) : OppgaveDetaljer() {
        val oppgaveTekst: String =
            """Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr $omsorgsmottaker i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."""

        override fun mottaker(): String {
            return omsorgsyter
        }

        override fun oppgavetekst(): String {
            return oppgaveTekst
        }
    }

    @JsonTypeName("FlereOmsorgytereMedLikeMyeOmsorg")
    data class FlereOmsorgytereMedLikeMyeOmsorg(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
        val annenOmsorgsyter: String,
    ) : OppgaveDetaljer() {
        val oppgaveTekst: String =
            """Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr $omsorgsmottaker. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr $annenOmsorgsyter mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene."""

        override fun mottaker(): String {
            return omsorgsyter
        }

        override fun oppgavetekst(): String {
            return oppgaveTekst
        }
    }

    /**
     * TODO
     * Ser ut som dette dekker caset "kunne ikke sendes til popp" + eventuelle caser som
     * ikke gjelder første leveår + flere mottakere
     * ikke gjelder andre år + flere mottakere
     *
     * Spørring:
     * "select g from GodskriveOmsorgspoeng g where"
     *                         + " g.skalSlettes is null"
     *                         + " and g.statusFerdigOk is :" + GodskriveOmsorgspoeng.NQ_PARAM_FALSE
     *                         + " and (g.kanGodskrives is :" + GodskriveOmsorgspoeng.NQ_PARAM_FALSE
     *                         + " 	  or g.sendtTilPopp is null or g.sendtTilPopp is :" + GodskriveOmsorgspoeng.NQ_PARAM_FALSE + " )"),
     */
    @JsonTypeName("UspesifisertFeilsituasjon")
    data class UspesifisertFeilsituasjon(
        val omsorgsyter: String,
    ) : OppgaveDetaljer() {
        val oppgaveTekst: String =
            """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."""

        override fun mottaker(): String {
            return omsorgsyter
        }

        override fun oppgavetekst(): String {
            return oppgaveTekst
        }
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
    val mottaker = detaljer.mottaker()
    val oppgavetekst = detaljer.oppgavetekst()

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
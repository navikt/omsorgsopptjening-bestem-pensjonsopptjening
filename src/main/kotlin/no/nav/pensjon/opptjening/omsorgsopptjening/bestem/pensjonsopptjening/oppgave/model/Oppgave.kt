package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class Oppgave {
    abstract val detaljer: OppgaveDetaljer
    abstract val behandlingId: UUID? //kan være null dersom alt feiler fra start
    abstract val meldingId: UUID
    abstract val statushistorikk: List<Status>

    val status: Status get() = statushistorikk.last()
    val mottaker: String get() = detaljer.mottaker()
    val oppgavetekst: Set<String> get() = detaljer.oppgavetekst()

    companion object Oppgavetekster {
        fun flereOmsorgsytereMedLikeMyeOmsorgFødselsår(omsorgsmottaker: String): String {
            return """Flere barnetrygdmottakere. Flere brukere mottar barnetrygd for samme barn $omsorgsmottaker i fødselsåret"""
        }

        fun flereOmsorgsytereMedLikeMyeOmsorg(omsorgsmottaker: String, annenOmsorgsyter: String): String {
            return """Flere barnetrygdmottakere. Flere brukere mottar like mye barnetrygd for samme barn $omsorgsmottaker. Bruker $annenOmsorgsyter mottok også barnetrygd"""
        }

        fun kunneIkkeBehandlesAutomatisk(): String {
            return """Kunne ikke behandle automatisk"""
        }

        fun perioderMedPliktigEllerFrivilligMedlemskap(omsorgsmottaker: String): String {
            return """Medlemskap. Bruker er pliktig eller frivillig medlem i utlandet i perioden hen mottar barnetrygd for barnet $omsorgsmottaker"""
        }

        fun kombinasjonAvFullOgDeltErTilstrekkelig(omsorgsmottaker: String): String {
            return """Delt barnetrygd. Bruker mottar barnetrygd i minst 6 måneder, men hele eller deler er delt barnetrygd for barnet $omsorgsmottaker"""
        }

        fun eøsSakMottarPensjonEllerUføretrygd(omsorgsmottaker: String): String {
            return """Bosatt EØS-land. Bruker er bosatt i EØS-land og mottar alderspensjon eller uføretrygd i perioden hen mottar barnetrygd for barnet $omsorgsmottaker"""
        }

        fun omsorgsyterErDød(omsorgsmottaker: String): String {
            return """Dødsfall. Vurder om omsorgsopptjening for barnet $omsorgsmottaker skal godskrives annen forelder"""
        }

        fun annenForelderInnvilgetOmsorgsopptjeningForAnnetFellesbarn(
            omsorgsmottaker: String,
            annenForelderOgBarn: Set<Pair<String, String>>
        ): String {
            val andreOmsorgsytere = annenForelderOgBarn
                .map { it.first }.joinToString(separator = ",")
            val omsorgsmottakere = annenForelderOgBarn
                .map { it.second }.plus(omsorgsmottaker).joinToString(separator = ",")

            return """Ulike felles barn. Foreldrene mottar barnetrygd for disse barna $omsorgsmottakere. Den andre forelderen $andreOmsorgsytere har egen oppgave."""
        }
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

        fun stoppet(begrunnelse: String?): Persistent {
            return copy(statushistorikk = statushistorikk + status.stoppet(begrunnelse))
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

        open fun stoppet(begrunnelse: String?): Status {
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

            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }

            override fun klar(): Status {
                return Klar()
            }

            override fun kansellert(begrunnelse: String, kanselleringsResultat: KanselleringsResultat): Status {
                return Kansellert(begrunnelse = begrunnelse, kanselleringsResultat = kanselleringsResultat)
            }

        }

        @JsonTypeName("Ferdig")
        data class Ferdig(
            val tidspunkt: Instant = Instant.now(),
            val oppgaveId: String,
        ) : Status() {
            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
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

            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
            }

            override fun klar(): Status {
                return Klar()
            }

            override fun kansellert(begrunnelse: String, kanselleringsResultat: KanselleringsResultat): Status {
                return Kansellert(begrunnelse = begrunnelse, kanselleringsResultat = kanselleringsResultat)
            }


        }

        @JsonTypeName("Feilet")
        data class Feilet(
            val tidspunkt: Instant = Instant.now(),
        ) : Status() {
            override fun stoppet(begrunnelse: String?): Stoppet {
                return Stoppet(begrunnelse = begrunnelse)
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
        OPPDATERING_FEILET,
        KANSELLERING_IKKE_NODVENDIG,
        OPPGAVENG_ER_ENDRET_I_PARALLELL,
    }
}
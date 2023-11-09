package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BrevÅrsak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed class Brev {
    abstract val behandlingId: UUID
    abstract val årsak: BrevÅrsak
    abstract val statushistorikk: List<Status>
    val status: Status get() = statushistorikk.last()

    data class Transient(
        override val behandlingId: UUID,
        override val årsak: BrevÅrsak,
    ) : Brev() {
        override val statushistorikk: List<Status> = listOf(Status.Klar())
    }

    /**
     * Plukker med seg en del informasjon knyttet til aktuell [behandlingId]
     */
    data class Persistent(
        val id: UUID,
        val opprettet: Instant,
        val omsorgsyter: String,
        val correlationId: CorrelationId,
        val innlesingId: InnlesingId,
        val omsorgsår: Int,
        val meldingId: UUID,
        override val behandlingId: UUID,
        override val årsak: BrevÅrsak,
        override val statushistorikk: List<Status>,
    ) : Brev() {

        fun ferdig(journalpost: Journalpost): Persistent {
            return copy(statushistorikk = statushistorikk + status.ferdig(journalpost))
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

        open fun ferdig(journalpost: Journalpost): Ferdig {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Ferdig")
        }

        open fun retry(melding: String): Status {
            throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Retry")
        }

        @JsonTypeName("Klar")
        data class Klar(
            val tidspunkt: Instant = Instant.now()
        ) : Status() {
            override fun ferdig(journalpost: Journalpost): Ferdig {
                return Ferdig(journalpost = journalpost.id)
            }

            override fun retry(melding: String): Status {
                return Retry(melding = melding)
            }
        }

        @JsonTypeName("Ferdig")
        data class Ferdig(
            val tidspunkt: Instant = Instant.now(),
            val journalpost: String,
        ) : Status()

        @JsonTypeName("Retry")
        data class Retry(
            val tidspunkt: Instant = Instant.now(),
            val antallForsøk: Int = 1,
            val maxAntallForsøk: Int = 3,
            val karanteneTil: Instant = tidspunkt.plus(5, ChronoUnit.HOURS),
            val melding: String,
        ) : Status() {
            override fun ferdig(journalpost: Journalpost): Ferdig {
                return Ferdig(journalpost = journalpost.id)
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
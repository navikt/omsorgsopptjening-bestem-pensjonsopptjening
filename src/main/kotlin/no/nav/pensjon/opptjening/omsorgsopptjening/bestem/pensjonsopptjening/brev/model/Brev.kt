package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Brev(
    val id: UUID? = null,
    val opprettet: Instant? = null,
    val omsorgsyter: String,
    val behandlingId: UUID?,
    val meldingId: UUID,
    val correlationId: CorrelationId,
    val statushistorikk: List<Status> = listOf(Status.Klar()),
    val innlesingId: InnlesingId,
    val omsorgsår: Int,
) {
    val status = statushistorikk.last()

    fun ferdig(journalpost: Journalpost): Brev {
        return copy(statushistorikk = statushistorikk + status.ferdig(journalpost))
    }

    fun retry(melding: String): Brev {
        return copy(statushistorikk = statushistorikk + status.retry(melding))
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
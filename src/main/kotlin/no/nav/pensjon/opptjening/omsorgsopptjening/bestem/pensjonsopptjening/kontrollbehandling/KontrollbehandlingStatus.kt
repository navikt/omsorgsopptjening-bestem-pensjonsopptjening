package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class KontrollbehandlingStatus {

    open fun ferdig(): Ferdig {
        throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Ferdig")
    }

    open fun retry(melding: String): KontrollbehandlingStatus {
        throw IllegalArgumentException("Kan ikke gå fra status:${this::class.java} til Retry")
    }

    @JsonTypeName("Klar")
    data class Klar(
        val tidspunkt: Instant = now()
    ) : KontrollbehandlingStatus() {
        override fun ferdig(): Ferdig {
            return Ferdig()
        }

        override fun retry(melding: String): KontrollbehandlingStatus {
            return Retry(melding = melding)
        }
    }

    @JsonTypeName("Ferdig")
    data class Ferdig(
        val tidspunkt: Instant = now()
    ) : KontrollbehandlingStatus()

    @JsonTypeName("Retry")
    data class Retry(
        val tidspunkt: Instant = now(),
        val antallForsøk: Int = 1,
        val maxAntallForsøk: Int = 3,
        val karanteneTil: Instant = tidspunkt.plus(5, ChronoUnit.HOURS),
        val melding: String,
    ) : KontrollbehandlingStatus() {
        override fun ferdig(): Ferdig {
            return Ferdig()
        }

        override fun retry(melding: String): KontrollbehandlingStatus {
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
    }

    @JsonTypeName("Feilet")
    data class Feilet(
        val tidspunkt: Instant = now(),
    ) : KontrollbehandlingStatus()
}
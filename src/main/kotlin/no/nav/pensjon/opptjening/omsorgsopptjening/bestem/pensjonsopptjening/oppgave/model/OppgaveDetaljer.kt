package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
sealed class OppgaveDetaljer {

    abstract fun mottaker(): String
    abstract fun oppgavetekst(): Set<String>

    @JsonTypeName("MottakerOgTekst")
    data class MottakerOgTekst(
        val oppgavemottaker: String,
        val oppgavetekst: Set<String>,
    ) : OppgaveDetaljer() {
        override fun mottaker(): String {
            return oppgavemottaker
        }

        // TODO: Fikse serialiseringen
        @JsonDeserialize(`as` = Set::class)
        @JsonSerialize(`as` = Set::class)
        override fun oppgavetekst(): Set<String> {
            return oppgavetekst
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class Oppgaveopplysninger {
    data class Generell(
        val oppgavemottaker: String,
        val oppgaveTekst: String
    ) : Oppgaveopplysninger()

    data object Ingen : Oppgaveopplysninger()
}
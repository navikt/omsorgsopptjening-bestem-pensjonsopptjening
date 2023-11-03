package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class Oppgaveopplysninger {
    data class ToOmsorgsytereMedLikeMangeMånederOmsorg(
        val oppgaveMottaker: String,
        val annenOmsorgsyter: String,
        val omsorgsmottaker: String,
        val omsorgsår: Int,
    ) : Oppgaveopplysninger()

    data object Ingen : Oppgaveopplysninger()
}
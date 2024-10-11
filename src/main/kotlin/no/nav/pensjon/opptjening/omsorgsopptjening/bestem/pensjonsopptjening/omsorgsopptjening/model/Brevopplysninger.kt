package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class Brevopplysninger {
    data class InfobrevOmsorgsyterForHjelpestønadsmottaker(
        val årsak: BrevÅrsak
    ) : Brevopplysninger()

    data object Ingen : Brevopplysninger()
}
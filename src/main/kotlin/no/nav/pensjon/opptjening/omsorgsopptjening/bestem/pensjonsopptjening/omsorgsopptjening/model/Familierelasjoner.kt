package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class Familierelasjoner(
    val relasjoner: List<Familierelasjon>
) {
    fun erForelder(fnr: String): Boolean {
        return relasjoner.singleOrNull {it.ident == fnr }?.erForelder() ?: false
    }

    fun erBarn(fnr: String): Boolean {
        return relasjoner.singleOrNull {it.ident == fnr }?.erBarn() ?: false
    }
}

data class Familierelasjon(
    val ident: String,
    val relasjon: Relasjon
){
    fun erForelder() = erMor() || erFar()
    fun erBarn() = relasjon == Relasjon.BARN
    private fun erFar() = relasjon == Relasjon.FAR
    private fun erMor() = relasjon == Relasjon.MOR || relasjon == Relasjon.MEDMOR
    enum class Relasjon {
        BARN,
        FAR,
        MOR,
        MEDMOR,
    }
}
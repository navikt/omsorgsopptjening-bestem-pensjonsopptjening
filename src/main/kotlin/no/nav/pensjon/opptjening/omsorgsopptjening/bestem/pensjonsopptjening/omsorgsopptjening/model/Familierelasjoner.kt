package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class Familierelasjoner(
    val relasjoner: List<Familierelasjon>
) {
    fun erForelder(fnr: String): Boolean {
        return finnForeldre().let { it.farEllerMedmor == fnr || it.mor == fnr }
    }

    fun erBarn(fnr: String): Boolean {
        return relasjoner.singleOrNull { it.ident == fnr }?.erBarn() ?: false
    }

    fun finnForeldre(): Foreldre {
        val far = relasjoner.singleOrNull { it.erFar() }
        val mor = relasjoner.singleOrNull { it.erMor() }
        val medmor = relasjoner.singleOrNull { it.erMedmor() }

        return when {
            far != null -> {
                Foreldre(
                    farEllerMedmor = far.ident,
                    mor = mor!!.ident,
                )
            }

            medmor != null -> {
                Foreldre(
                    farEllerMedmor = medmor.ident,
                    mor = mor!!.ident
                )
            }

            else -> throw RuntimeException("Klarte ikke å identifisere foreldre")
        }
    }
}

data class Familierelasjon(
    val ident: String,
    val relasjon: Relasjon
) {
    fun erBarn() = relasjon == Relasjon.BARN
    fun erFar() = relasjon == Relasjon.FAR
    fun erMor() = relasjon == Relasjon.MOR
    fun erMedmor() = relasjon == Relasjon.MEDMOR
    enum class Relasjon {
        BARN,
        FAR,
        MOR,
        MEDMOR,
    }
}

data class Foreldre(
    val farEllerMedmor: String,
    val mor: String,
) {

}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class Familierelasjoner(
    val relasjoner: List<Familierelasjon>
) {
    fun erForelder(fnr: String): Boolean {
        return finnForeldre().let {
            when (it) {
                is Foreldre.Identifisert -> it.farEllerMedmor.ident == fnr || it.mor.ident == fnr
                is Foreldre.Ukjent -> false
            }
        }
    }

    fun erBarn(fnr: String): Boolean {
        return relasjoner.singleOrNull { it.ident.ident == fnr }?.erBarn() ?: false
    }

    fun finnForeldre(): Foreldre {
        val far = relasjoner.singleOrNull { it.erFar() }
        val mor = relasjoner.singleOrNull { it.erMor() }
        val medmor = relasjoner.singleOrNull { it.erMedmor() }

        return when {
            far != null && far.ident is Ident.FolkeregisterIdent && mor != null && mor.ident is Ident.FolkeregisterIdent -> {
                Foreldre.Identifisert(
                    farEllerMedmor = far.ident,
                    mor = mor.ident,
                )
            }

            medmor != null && medmor.ident is Ident.FolkeregisterIdent && mor != null && mor.ident is Ident.FolkeregisterIdent -> {
                Foreldre.Identifisert(
                    farEllerMedmor = medmor.ident,
                    mor = mor.ident
                )
            }

            else -> {
                Foreldre.Ukjent
            }
        }
    }
}

data class Familierelasjon(
    val ident: Ident,
    val relasjon: Relasjon
) {
    constructor(
        ident: String,
        relasjon: Relasjon
    ) : this(
        when (ident == Ident.IDENT_UKJENT) {
            true -> Ident.Ukjent
            false -> Ident.FolkeregisterIdent.Gjeldende(ident)
        },
        relasjon
    )

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

sealed class Foreldre {
    data class Identifisert(
        val farEllerMedmor: Ident.FolkeregisterIdent,
        val mor: Ident.FolkeregisterIdent,
    ) : Foreldre()

    data object Ukjent : Foreldre()
}

sealed class Ident {
    abstract val ident: String

    sealed class FolkeregisterIdent : Ident() {

        data class Gjeldende(
            override val ident: String
        ) : FolkeregisterIdent()

        data class Historisk(
            override val ident: String
        ) : FolkeregisterIdent()
    }

    data object Ukjent : Ident() {
        override val ident: String = IDENT_UKJENT
    }

    companion object {
        const val IDENT_UKJENT = "IDENT_UKJENT"
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkårsvurderingUtfall {

    abstract fun lovhenvisning(): Set<Lovhenvisning>
    sealed class Innvilget : VilkårsvurderingUtfall() {
        data class EnkeltParagraf(val lovhenvisning: Set<Lovhenvisning>) : Innvilget() {
            override fun lovhenvisning(): Set<Lovhenvisning> {
                return lovhenvisning
            }
        }
    }

    sealed class Avslag : VilkårsvurderingUtfall() {
        data class EnkeltParagraf(val lovhenvisning: Set<Lovhenvisning>) : Avslag() {
            override fun lovhenvisning(): Set<Lovhenvisning> {
                return lovhenvisning
            }
        }
    }

    fun erInnvilget(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> true
        }
    }

}

sealed class BehandlingUtfall {
    fun erInnvilget(): Boolean {
        return when (this) {
            is AutomatiskGodskrivingUtfall.Avslag -> false
            is AutomatiskGodskrivingUtfall.Innvilget -> true
        }
    }
}

sealed class AutomatiskGodskrivingUtfall : BehandlingUtfall() {

    abstract val oppsummering: Behandlingsoppsummering

    data class Innvilget(override val oppsummering: Behandlingsoppsummering) : AutomatiskGodskrivingUtfall()
    data class Avslag(override val oppsummering: Behandlingsoppsummering) : AutomatiskGodskrivingUtfall()
}

data class Behandlingsoppsummering(
    val paragrafOppsummering: List<ParagrafOppsummering>
)

data class ParagrafOppsummering(
    val paragrafUtfall: Pair<Lovhenvisning, Boolean>
)
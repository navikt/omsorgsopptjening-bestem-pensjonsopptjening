package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkårsvurderingUtfall {

    abstract fun paragrafer(): Set<Paragraf>
    sealed class Innvilget : VilkårsvurderingUtfall() {
        data class EnkeltParagraf(val paragraf: Paragraf) : Innvilget() {
            override fun paragrafer(): Set<Paragraf> {
                return setOf(paragraf)
            }
        }
    }

    sealed class Avslag : VilkårsvurderingUtfall() {
        data class EnkeltParagraf(val paragraf: Paragraf) : Avslag() {
            override fun paragrafer(): Set<Paragraf> {
                return setOf(paragraf)
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
    val paragrafUtfall: Pair<Paragraf, Boolean>
)
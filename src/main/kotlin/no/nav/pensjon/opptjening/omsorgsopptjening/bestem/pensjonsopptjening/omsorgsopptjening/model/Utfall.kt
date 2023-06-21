package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkårsvurderingUtfall {

    abstract fun henvisninger(): Set<Henvisning>
    sealed class Innvilget : VilkårsvurderingUtfall() {

        data class Vilkår(val henvisninger: Set<Henvisning>) : Innvilget() {
            override fun henvisninger(): Set<Henvisning> {
                return henvisninger
            }

            companion object {
                fun from(referanser: Set<Referanse>): Vilkår {
                    return Vilkår(referanser.map { it.henvisning }.toSet())
                }
            }
        }
    }

    sealed class Avslag : VilkårsvurderingUtfall() {
        data class Vilkår(val henvisninger: Set<Henvisning>) : Avslag() {
            override fun henvisninger(): Set<Henvisning> {
                return henvisninger
            }

            companion object {
                fun from(referanser: Set<Referanse>): Vilkår {
                    return Vilkår(referanser.map { it.henvisning }.toSet())
                }
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
    object Innvilget : AutomatiskGodskrivingUtfall()

    sealed class Avslag : AutomatiskGodskrivingUtfall()
    object AvslagMedOppgave : Avslag()
    object AvslagUtenOppgave : Avslag()
}
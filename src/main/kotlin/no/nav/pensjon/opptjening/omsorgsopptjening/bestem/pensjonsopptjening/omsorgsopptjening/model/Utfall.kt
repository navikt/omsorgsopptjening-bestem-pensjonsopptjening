package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkårsvurderingUtfall {

    sealed class Innvilget : VilkårsvurderingUtfall() {

        data class Vilkår(val henvisninger: Set<JuridiskHenvisning>) : Innvilget() {

            companion object {
                fun from(referanser: Set<Referanse>): Vilkår {
                    return Vilkår(referanser.map { it.henvisning }.toSet())
                }
            }
        }
    }

    sealed class Avslag : VilkårsvurderingUtfall() {
        data class Vilkår(val henvisninger: Set<JuridiskHenvisning>) : Avslag() {

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
    object Innvilget : BehandlingUtfall()
    object Avslag : BehandlingUtfall()

    fun erInnvilget(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> true
        }
    }
}
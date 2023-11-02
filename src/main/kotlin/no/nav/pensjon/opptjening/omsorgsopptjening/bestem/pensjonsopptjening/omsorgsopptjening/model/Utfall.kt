package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave

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
    data object Innvilget : BehandlingUtfall()
    data object Avslag : BehandlingUtfall()
    data class Manuell (val oppgave:Oppgave.Transient?) : BehandlingUtfall()

    fun erInnvilget(): Boolean {
        return when (this) {
            is Avslag -> false
            is Innvilget -> true
            is Manuell -> false
        }
    }
}
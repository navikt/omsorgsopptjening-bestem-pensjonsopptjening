package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
sealed class VilkårsvurderingUtfall {
    sealed class Innvilget : VilkårsvurderingUtfall()
    sealed class Avslag: VilkårsvurderingUtfall() {
        abstract val årsaker: List<AvslagÅrsak>
    }

}

sealed class BehandlingUtfall {
    fun erInnvilget(): Boolean {
        return when(this){
            is AutomatiskGodskrivingUtfall.Avslag -> false
            is AutomatiskGodskrivingUtfall.Innvilget -> true
        }
    }
}
sealed class AutomatiskGodskrivingUtfall : BehandlingUtfall() {
    data class Innvilget(val omsorgsmottaker: PersonMedFødselsår) : AutomatiskGodskrivingUtfall()
    data class Avslag(val omsorgsmottaker: PersonMedFødselsår, val årsaker: List<AvslagÅrsak>) : AutomatiskGodskrivingUtfall()
}
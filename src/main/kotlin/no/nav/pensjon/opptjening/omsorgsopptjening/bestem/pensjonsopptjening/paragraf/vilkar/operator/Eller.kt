package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering

class Eller<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Et av vilkårene må være sanne.",
        begrunnelseForInnvilgelse = "Et av vilkårene var sanne.",
        begrunnesleForAvslag = "Ingen av vilkårene var sanne."
    ),
    utfallsFunksjon = ellerFunksjon
) {

    companion object {
        private val ellerFunksjon = fun(vilkarsVurdering: List<VilkarsVurdering<*>>): Utfall {
            if(vilkarsVurdering.isEmpty()) return Utfall.AVSLAG
            val utfall = vilkarsVurdering.map { it.utfall }

            return when {
                utfall.any { it == Utfall.INVILGET } -> Utfall.INVILGET
                utfall.all { it == Utfall.AVSLAG } -> Utfall.AVSLAG
                else -> Utfall.SAKSBEHANDLING
            }
        }

        fun eller(vararg vilkarsVurderinger: VilkarsVurdering<*>) = Eller<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())

        fun eller(vilkarsVurderinger: List<VilkarsVurdering<*>>) = Eller<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())
    }
}
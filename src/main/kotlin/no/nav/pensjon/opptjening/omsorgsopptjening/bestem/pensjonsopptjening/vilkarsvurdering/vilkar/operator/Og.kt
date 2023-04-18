package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering

class Og<T : VilkarsVurdering<*>> : Vilkar<List<T>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Alle vilkår må være sanne.",
        begrunnelseForInnvilgelse = "Alle vilkår var sanne.",
        begrunnesleForAvslag = "Alle vilkår var ikke sanne."
    ),
    utfallsFunksjon = ogFunksjon
) {

    companion object {

        private val ogFunksjon = fun(vilkarsVurdering: List<VilkarsVurdering<*>>): Utfall {
            if(vilkarsVurdering.isEmpty()) return Utfall.AVSLAG
            val utfall = vilkarsVurdering.map { it.utfall }

            return when {
                utfall.all { it == Utfall.INVILGET } -> Utfall.INVILGET
                utfall.any { it == Utfall.MANGLER_ANNEN_OMSORGSYTER } -> Utfall.MANGLER_ANNEN_OMSORGSYTER
                utfall.any { it == Utfall.SAKSBEHANDLING } -> Utfall.SAKSBEHANDLING
                else -> Utfall.AVSLAG
            }
        }

        fun og(vararg vilkarsVurderinger: VilkarsVurdering<*>) = Og<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())
    }
}
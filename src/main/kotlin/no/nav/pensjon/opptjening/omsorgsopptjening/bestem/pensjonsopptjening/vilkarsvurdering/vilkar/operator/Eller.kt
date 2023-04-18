package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.VilkarsVurdering

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
            if (vilkarsVurdering.isEmpty()) return Utfall.AVSLAG
            val utfall = vilkarsVurdering.map { it.utfall }

            return when {
                utfall.any { it == Utfall.INVILGET } -> Utfall.INVILGET
                utfall.all { it == Utfall.AVSLAG } -> Utfall.AVSLAG
                utfall.any { it == Utfall.MANGLER_ANNEN_OMSORGSYTER } -> Utfall.MANGLER_ANNEN_OMSORGSYTER
                else -> Utfall.SAKSBEHANDLING
            }
        }

        fun eller(vararg vilkarsVurderinger: VilkarsVurdering<*>) = Eller<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())

        fun eller(vilkarsVurderinger: List<VilkarsVurdering<*>>) = Eller<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())

        fun <Input, Vurdering : VilkarsVurdering<*>> Iterable<Input>.minstEn(mappingFunction: (Input) -> Vurdering) = eller(map { mappingFunction.invoke(it) })
    }
}
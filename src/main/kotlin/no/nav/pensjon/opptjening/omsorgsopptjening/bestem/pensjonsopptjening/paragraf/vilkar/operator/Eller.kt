package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering

class Eller<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Et av vilkårene må være sanne.",
        begrunnelseForInnvilgelse = "Et av vilkårene var sanne.",
        begrunnesleForAvslag = "Ingen av vilkårene var sanne."
    ),
    avgjorelsesFunksjon = ellerFunksjon
) {

    companion object {
        private val ellerFunksjon = fun(vilkarsVurdering: List<VilkarsVurdering<*>>): Avgjorelse {
            if(vilkarsVurdering.isEmpty()) return Avgjorelse.AVSLAG
            val avgjorelser = vilkarsVurdering.map { it.avgjorelse }

            return when {
                avgjorelser.any { it == Avgjorelse.INVILGET } -> Avgjorelse.INVILGET
                avgjorelser.all { it == Avgjorelse.AVSLAG } -> Avgjorelse.AVSLAG
                else -> Avgjorelse.SAKSBEHANDLING
            }
        }

        fun eller(vararg vilkarsVurderinger: VilkarsVurdering<*>) = Eller<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())

        fun eller(vilkarsVurderinger: List<VilkarsVurdering<*>>) = Eller<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())
    }
}
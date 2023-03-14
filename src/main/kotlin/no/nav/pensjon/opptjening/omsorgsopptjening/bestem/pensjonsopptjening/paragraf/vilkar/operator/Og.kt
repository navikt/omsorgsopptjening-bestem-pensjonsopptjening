package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering

class Og<T : VilkarsVurdering<*>> : Vilkar<List<T>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Alle vilkår må være sanne.",
        begrunnelseForInnvilgelse = "Alle vilkår var sanne.",
        begrunnesleForAvslag = "Alle vilkår var ikke sanne."
    ),
    avgjorelsesFunksjon = ogFunksjon
) {

    companion object {

        private val ogFunksjon = fun(vilkarsVurdering: List<VilkarsVurdering<*>>): Avgjorelse {
            val avgjorelser = vilkarsVurdering.map { it.utfor().avgjorelse }

            return when {
                avgjorelser.all { it == Avgjorelse.INVILGET } -> Avgjorelse.INVILGET
                avgjorelser.any { it == Avgjorelse.SAKSBEHANDLING } -> Avgjorelse.SAKSBEHANDLING
                else -> Avgjorelse.AVSLAG
            }
        }

        fun og(vararg vilkarsVurderinger: VilkarsVurdering<*>) = Og<VilkarsVurdering<*>>().vilkarsVurder(vilkarsVurderinger.toList())
    }
}
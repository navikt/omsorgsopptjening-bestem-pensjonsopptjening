package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering

class Og<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Alle vilkår må være sanne.",
        begrunnelseForInnvilgelse = "Alle vilkår var sanne.",
        begrunnesleForAvslag = "Alle vilkår var ikke sanne."
    ),
    kalkulerAvgjorelse = { vilkarsVurdering -> vilkarsVurdering.all { it.utforVilkarsVurdering().avgjorelse } }
) {

    companion object {
        fun og(vararg vilkar: VilkarsVurdering<*>) = Og<VilkarsVurdering<*>>().utforVilkarsVurdering(vilkar.toList())
    }
}
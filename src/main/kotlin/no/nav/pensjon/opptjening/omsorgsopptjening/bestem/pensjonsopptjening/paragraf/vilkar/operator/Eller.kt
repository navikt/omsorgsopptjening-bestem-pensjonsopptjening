package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsVurdering

class Eller<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Et av vilkårene må være sanne.",
        begrunnelseForInnvilgelse = "Et av vilkårene var sanne.",
        begrunnesleForAvslag = "Ingen av vilkårene var sanne."
    ),
    kalkulerAvgjorelse = { vilkarsVurdering -> vilkarsVurdering.any { it.utforVilkarsVurdering().avgjorelse } }
) {

    companion object {
        fun eller(vararg vilkar: VilkarsVurdering<*>) = Eller<VilkarsVurdering<*>>().utforVilkarsVurdering(vilkar.toList())
    }
}
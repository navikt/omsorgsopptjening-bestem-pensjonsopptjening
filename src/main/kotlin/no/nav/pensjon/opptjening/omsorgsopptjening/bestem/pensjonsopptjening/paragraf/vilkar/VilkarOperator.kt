package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar


class Og<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Alle vilkår må være sanne.",
        begrunnelseForInnvilgelse = "Alle vilkår var sanne.",
        begrunnesleForAvslag = "Alle vilkår var ikke sanne."
    ),
    oppfyllerRegler = { vilkarsVurdering -> vilkarsVurdering.all { it.utforVilkarsVurdering().oppFyllerRegel } }
) {

    companion object {
        fun og(vararg vilkar: VilkarsVurdering<*>) = Og<VilkarsVurdering<*>>().utforVilkarsVurdering(vilkar.toList())
    }
}

class Eller<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Et av vilkårene må være sanne.",
        begrunnelseForInnvilgelse = "Et av vilkårene var sanne.",
        begrunnesleForAvslag = "Ingen av vilkårene var sanne."
    ),
    oppfyllerRegler = { vilkarsVurdering -> vilkarsVurdering.any { it.utforVilkarsVurdering().oppFyllerRegel } }
) {

    companion object {
        fun eller(vararg vilkar: VilkarsVurdering<*>) = Eller<VilkarsVurdering<*>>().utforVilkarsVurdering(vilkar.toList())
    }
}
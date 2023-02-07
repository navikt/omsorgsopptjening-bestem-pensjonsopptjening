package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar


class Og<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Alle brukte regler må være de sanne.",
        begrunnelseForInnvilgelse = "Alle regler var sanne.",
        begrunnesleForAvslag = "Alle regler var ikke sanne."
    ),
    oppfyllerRegler = { vilkarsVurdering -> vilkarsVurdering.all { it.utførVilkarsVurdering().oppFyllerRegel } }
) {

    companion object {
        fun og(vararg vilkar: VilkarsVurdering<*>) = Og<VilkarsVurdering<*>>().utførVilkarsVurdering(vilkar.toList())
    }
}

class Eller<T : VilkarsVurdering<*>> private constructor() : Vilkar<List<T>>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Alle brukte regler må være de sanne.",
        begrunnelseForInnvilgelse = "Alle regler var sanne.",
        begrunnesleForAvslag = "Alle regler var ikke sanne."
    ),
    oppfyllerRegler = { vilkarsVurdering -> vilkarsVurdering.any { it.utførVilkarsVurdering().oppFyllerRegel } }
) {

    companion object {
        fun eller(vararg vilkar: VilkarsVurdering<*>) = Eller<VilkarsVurdering<*>>().utførVilkarsVurdering(vilkar.toList())
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar

class Og private constructor(vilkarsVurderinger: List<VilkarsVurdering<*>>) :
    VilkarsVurdering<List<VilkarsVurdering<*>>>(
        vilkarsInformasjon = VilkarsInformasjon(
            beskrivelse = "Alle brukte regler må være de sanne.",
            begrunnelseForInnvilgelse = "Alle regler var sanne.",
            begrunnesleForAvslag = "Alle regler var ikke sanne."
        ),
        inputVerdi = vilkarsVurderinger,
        oppfyllerRegler = { vilkar -> vilkar.map { it.utførVilkarsVurdering() }.all { it.oppFyllerRegel } }
    ) {
    companion object {
        fun og(vararg vilkar: VilkarsVurdering<*>) = Og(vilkar.toList())
    }
}

class Eller private constructor(vilkarsVurderinger: List<VilkarsVurdering<*>>) :
    VilkarsVurdering<List<VilkarsVurdering<*>>>(
        VilkarsInformasjon(
            beskrivelse = "En av Reglene som blir brukt må være sann.",
            begrunnelseForInnvilgelse = "En av reglene var sanne",
            begrunnesleForAvslag = "Ingen av reglene var sanne"
        ),
        inputVerdi = vilkarsVurderinger,
        oppfyllerRegler = { vilkar -> vilkar.map { it.utførVilkarsVurdering() }.any { it.oppFyllerRegel } }
    ) {

    companion object {
        fun eller(vararg vilkar: VilkarsVurdering<*>) = Eller(vilkar.toList())
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar

class Og private constructor(input: List<VilkarsResultat>) : VilkarsVurdering<List<VilkarsResultat>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Alle brukte regler må være de sanne.",
        begrunnelseForInnvilgelse = "Alle regler var sanne.",
        begrunnesleForAvslag = "Alle regler var ikke sanne."
    ),
    inputVerdi = input,
    oppfyllerRegler = { resultater -> resultater.all { it.oppFyllerRegel } }
) {
    companion object {
        fun og(vararg regler: VilkarsVurdering<*>) = Og(regler.map { it.utførVilkarsVurdering() })
    }
}

class Eller private constructor(input: List<VilkarsResultat>) : VilkarsVurdering<List<VilkarsResultat>>(
    VilkarsInformasjon(
        beskrivelse = "En av Reglene som blir brukt må være sann.",
        begrunnelseForInnvilgelse = "En av reglene var sanne",
        begrunnesleForAvslag = "Ingen av reglene var sanne"
    ),
    inputVerdi = input,
    oppfyllerRegler = { resultater -> resultater.any { it.oppFyllerRegel } }
) {

    companion object {
        fun eller(vararg regler: VilkarsVurdering<*>) = Eller(regler.map { it.utførVilkarsVurdering() })
    }
}
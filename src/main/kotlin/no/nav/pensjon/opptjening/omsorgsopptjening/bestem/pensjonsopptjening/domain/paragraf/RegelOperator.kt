package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf

class Og private constructor(input: List<RegelResultat>) : Regel<List<RegelResultat>>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Alle brukte regler må være de sanne.",
        begrunnelseForInnvilgelse = "Alle regler var sanne.",
        begrunnesleForAvslag = "Alle regler var ikke sanne."
    ),
    inputVerdi = input,
    oppfyllerRegler = { resultater -> resultater.all { it.oppFyllerRegel } }
) {

    companion object {
        fun og(vararg regler: Regel<*>) = Og(regler.map { it.brukRegel() })
    }
}

class Eller private constructor(input: List<RegelResultat>) : Regel<List<RegelResultat>>(
    RegelInformasjon(
        beskrivelse = "En av Reglene som blir brukt må være sann.",
        begrunnelseForInnvilgelse = "En av reglene var sanne",
        begrunnesleForAvslag = "Ingen av reglene var sanne"
    ),
    inputVerdi = input,
    oppfyllerRegler = { resultater -> resultater.any { it.oppFyllerRegel } }
) {

    companion object {
        fun eller(vararg regler: Regel<*>) = Eller(regler.map { it.brukRegel() })
    }
}
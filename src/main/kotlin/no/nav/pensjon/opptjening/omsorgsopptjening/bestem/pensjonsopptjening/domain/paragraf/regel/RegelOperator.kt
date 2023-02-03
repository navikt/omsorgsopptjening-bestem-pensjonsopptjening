package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.regel

class Og private constructor(input: List<RegelResultat>) : RegelInstanse<List<RegelResultat>>(
    regelInformasjon = RegelInformasjon(
        beskrivelse = "Alle brukte regler må være de sanne.",
        begrunnelseForInnvilgelse = "Alle regler var sanne.",
        begrunnesleForAvslag = "Alle regler var ikke sanne."
    ),
    inputVerdi = input,
    oppfyllerRegler = { resultater -> resultater.all { it.oppFyllerRegel } }
) {

    companion object {
        fun og(vararg regler: RegelInstanse<*>) = Og(regler.map { it.bruk() })
    }
}

class Eller private constructor(input: List<RegelResultat>) : RegelInstanse<List<RegelResultat>>(
    RegelInformasjon(
        beskrivelse = "En av Reglene som blir brukt må være sann.",
        begrunnelseForInnvilgelse = "En av reglene var sanne",
        begrunnesleForAvslag = "Ingen av reglene var sanne"
    ),
    inputVerdi = input,
    oppfyllerRegler = { resultater -> resultater.any { it.oppFyllerRegel } }
) {

    companion object {
        fun eller(vararg regler: RegelInstanse<*>) = Eller(regler.map { it.bruk() })
    }
}
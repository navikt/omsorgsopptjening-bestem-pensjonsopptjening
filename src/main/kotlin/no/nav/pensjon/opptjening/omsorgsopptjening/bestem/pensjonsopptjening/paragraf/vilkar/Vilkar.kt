package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar


open class Vilkar<Grunnlag : Any>(
    val regelInformasjon: RegelInformasjon,
    val oppfyllerRegler: (Grunnlag) -> Boolean,
) {
    fun vilkarsVurder(grunnlag: Grunnlag) = VilkarsVurdering(vilkar = this, grunnlag = grunnlag)

    fun utførVilkarsVurdering(input: Grunnlag) = VilkarsVurdering(this, input).utførVilkarsVurdering()
}

data class RegelInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar


open class Vilkar<Grunnlag : Any>(
    val vilkarsInformasjon: VilkarsInformasjon,
    val avgjorelsesFunksjon: (Grunnlag) -> Avgjorelse,
) {
    fun vilkarsVurder(grunnlag: Grunnlag) = VilkarsVurdering(vilkar = this, grunnlag = grunnlag)

    fun utforVilkarsVurdering(input: Grunnlag) = VilkarsVurdering(this, input).utforVilkarsVurdering()
}

data class VilkarsInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)
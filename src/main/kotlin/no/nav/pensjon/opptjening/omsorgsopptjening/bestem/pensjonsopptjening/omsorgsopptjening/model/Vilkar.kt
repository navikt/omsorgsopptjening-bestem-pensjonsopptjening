package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


abstract class Vilkar<Grunnlag : Any>(
    val vilkarsInformasjon: VilkarsInformasjon,
    val utfallsFunksjon: Vilkar<Grunnlag>.(Grunnlag) -> VilkårsvurderingUtfall,
) {
    abstract fun vilkarsVurder(grunnlag: Grunnlag): VilkarsVurdering<Grunnlag>
}

data class VilkarsInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)
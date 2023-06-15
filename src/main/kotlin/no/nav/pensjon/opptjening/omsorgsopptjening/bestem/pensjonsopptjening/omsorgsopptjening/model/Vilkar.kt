package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


abstract class Vilkar<Grunnlag: Any>(
    val utfallsFunksjon: Vilkar<Grunnlag>.(Grunnlag) -> VilkårsvurderingUtfall,
) {
    abstract fun vilkarsVurder(grunnlag: Grunnlag): VilkarsVurdering<Grunnlag>
}

abstract class ParagrafVilkår<T: ParagrafGrunnlag>(
    val paragrafer: Set<Paragraf>,
    utfallsFunksjon: Vilkar<T>.(T) -> VilkårsvurderingUtfall
) : Vilkar<T>(utfallsFunksjon)

abstract class ParagrafGrunnlag

data class VilkarsInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

 class OmsorgsYterOver16Ar : Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Medlemmet er under 17 år.",
        begrunnelseForInnvilgelse = "Medlemmet er over 16 år.",
    ),
    utfallsFunksjon = `Person er over 16 ar`,
) {
    companion object {
        private val `Person er over 16 ar` =
            fun Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>.(input: OmsorgsyterOgOmsorgsårGrunnlag): VilkårsvurderingUtfall =
                if (input.omsorgsyter.alder(input.omsorgsAr) > 16) {
                    OmsorgsyterOver16ArInnvilget(this.vilkarsInformasjon.begrunnelseForInnvilgelse)
                } else {
                    OmsorgsyterOver16ArAvslag(årsaker = listOf(AvslagÅrsak.OMSORGSYTER_IKKE_OVER_16))
                }
    }

    override fun vilkarsVurder(grunnlag: OmsorgsyterOgOmsorgsårGrunnlag): OmsorgsyterOver16ArVurdering {
        return OmsorgsyterOver16ArVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsyterOver16ArVurdering(
    override val vilkar: Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>,
    override val grunnlag: OmsorgsyterOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<OmsorgsyterOgOmsorgsårGrunnlag>()

data class OmsorgsyterOver16ArInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsyterOver16ArAvslag(override val årsaker: List<AvslagÅrsak>) : VilkårsvurderingUtfall.Avslag()
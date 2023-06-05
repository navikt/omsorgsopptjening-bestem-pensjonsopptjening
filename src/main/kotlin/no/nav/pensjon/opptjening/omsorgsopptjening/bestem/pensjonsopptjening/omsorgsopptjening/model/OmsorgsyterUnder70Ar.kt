package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterUnder70Ar : Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd til og med det året vedkommende fyller 69 år.",
        begrunnesleForAvslag = "Medlemmet er over 69 år.",
        begrunnelseForInnvilgelse = "Medlemmet er under 70 år.",
    ),
    utfallsFunksjon = `Medlemmet er under 70 ar`,
) {
    companion object {
        private val `Medlemmet er under 70 ar` =
            fun Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>.(input: OmsorgsyterOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
                return if (input.omsorgsyter.alder(input.omsorgsAr) < 70) {
                    OmsorgsyterUnder70ArInnvilget(
                        årsak = this.vilkarsInformasjon.begrunnelseForInnvilgelse
                    )
                } else {
                    OmsorgsyterUnder70ArAvslag(årsaker = listOf(AvslagÅrsak.OMSORGSYTER_OVER_69))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: OmsorgsyterOgOmsorgsårGrunnlag): OmsorgsyterUnder70Vurdering {
        return OmsorgsyterUnder70Vurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsyterUnder70Vurdering(
    override val vilkar: Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>,
    override val grunnlag: OmsorgsyterOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<OmsorgsyterOgOmsorgsårGrunnlag>()

data class OmsorgsyterUnder70ArInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsyterUnder70ArAvslag(override val årsaker: List<AvslagÅrsak>) : VilkårsvurderingUtfall.Avslag()
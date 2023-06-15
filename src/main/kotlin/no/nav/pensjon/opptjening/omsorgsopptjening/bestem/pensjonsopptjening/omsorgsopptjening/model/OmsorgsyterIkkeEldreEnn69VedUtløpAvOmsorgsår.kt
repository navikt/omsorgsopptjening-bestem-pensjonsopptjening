package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår : Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd til og med det året vedkommende fyller 69 år.",
        begrunnesleForAvslag = "Medlemmet er over 69 år.",
        begrunnelseForInnvilgelse = "Medlemmet er under 70 år.",
    ),
    utfallsFunksjon = vurderUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>.(input: OmsorgsyterOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
                return if (input.omsorgsyter.alderVedUtløpAv(input.omsorgsAr) <= 69) {
                    OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårInnvilget(
                        årsak = this.vilkarsInformasjon.begrunnelseForInnvilgelse
                    )
                } else {
                    OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårAvslag(årsaker = listOf(AvslagÅrsak.OMSORGSYTER_ELDRE_ENN_69_VED_UTGANG_AV_OMSORGSÅR))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: OmsorgsyterOgOmsorgsårGrunnlag): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering {
        return OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
    override val vilkar: Vilkar<OmsorgsyterOgOmsorgsårGrunnlag>,
    override val grunnlag: OmsorgsyterOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<OmsorgsyterOgOmsorgsårGrunnlag>()

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()
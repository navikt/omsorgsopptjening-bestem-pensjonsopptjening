package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterFylt17VedUtløpAvOmsorgsår : Vilkar<PersonOgOmsorgsårGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Medlemmet er under 17 år.",
        begrunnelseForInnvilgelse = "Medlemmet er over 16 år.",
    ),
    utfallsFunksjon = vurderUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun Vilkar<PersonOgOmsorgsårGrunnlag>.(input: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall =
                if (input.person.alderVedUtløpAv(input.omsorgsAr) >= 17) {
                    OmsorgsyterFylt17ÅrInnvilget(this.vilkarsInformasjon.begrunnelseForInnvilgelse)
                } else {
                    OmsorgsyterFylt17ÅrAvslag(årsaker = listOf(AvslagÅrsak.OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR))
                }
    }

    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterFylt17ÅrVurdering {
        return OmsorgsyterFylt17ÅrVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsyterFylt17ÅrVurdering(
    override val vilkar: Vilkar<PersonOgOmsorgsårGrunnlag>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<PersonOgOmsorgsårGrunnlag>()

data class OmsorgsyterFylt17ÅrInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsyterFylt17ÅrAvslag(override val årsaker: List<AvslagÅrsak>) : VilkårsvurderingUtfall.Avslag()
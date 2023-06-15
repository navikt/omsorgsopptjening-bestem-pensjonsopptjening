package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>(
    paragrafer = setOf(Paragraf.A),
    utfallsFunksjon = vurderUtfall as Vilkar<PersonOgOmsorgsårGrunnlag>.(PersonOgOmsorgsårGrunnlag) -> VilkårsvurderingUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun ParagrafVilkår<PersonOgOmsorgsårGrunnlag>.(input: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
                return if (input.person.alderVedUtløpAv(input.omsorgsAr) <= 69) {
                    OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårInnvilget(
                        årsak = ""
                    )
                } else {
                    OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårAvslag(årsaker = listOf(AvslagÅrsak.OMSORGSYTER_ELDRE_ENN_69_VED_UTGANG_AV_OMSORGSÅR))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering {
        return OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()
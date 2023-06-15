package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterFylt17VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>(
    paragrafer = setOf(Paragraf.A),
    utfallsFunksjon = vurderUtfall as Vilkar<PersonOgOmsorgsårGrunnlag>.(PersonOgOmsorgsårGrunnlag) -> VilkårsvurderingUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun ParagrafVilkår<PersonOgOmsorgsårGrunnlag>.(input: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall =
                if (input.person.alderVedUtløpAv(input.omsorgsAr) >= 17) {
                    OmsorgsyterFylt17ÅrInnvilget("")
                } else {
                    OmsorgsyterFylt17ÅrAvslag(årsaker = listOf(AvslagÅrsak.OMSORGSYTER_IKKE_FYLLT_17_VED_UTGANG_AV_OMSORGSÅR))
                }
    }

    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterFylt17ÅrVurdering {
        return OmsorgsyterFylt17ÅrVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsyterFylt17ÅrVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()

data class OmsorgsyterFylt17ÅrInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsyterFylt17ÅrAvslag(override val årsaker: List<AvslagÅrsak>) : VilkårsvurderingUtfall.Avslag()
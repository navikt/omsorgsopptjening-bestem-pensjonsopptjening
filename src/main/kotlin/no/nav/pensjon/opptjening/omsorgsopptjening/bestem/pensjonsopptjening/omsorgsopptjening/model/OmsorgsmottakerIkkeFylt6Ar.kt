package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsmottakerIkkeFylt6Ar : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>(
    paragrafer = setOf(Paragraf.A),
    utfallsFunksjon = vurderUtfall as Vilkar<PersonOgOmsorgsårGrunnlag>.(PersonOgOmsorgsårGrunnlag) -> VilkårsvurderingUtfall,
) {

    companion object {
        private val vurderUtfall =
            fun ParagrafVilkår<PersonOgOmsorgsårGrunnlag>.(input: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
                if(input.alderMottaker(mellom = 0..5)) {
                    return OmsorgsmottakerIkkeFylt6ArInnvilget("")
                } else {
                    return OmsorgsmottakerIkkeFylt6ArAvslag(listOf(AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsmottakerIkkeFylt6ArVurdering {
        return OmsorgsmottakerIkkeFylt6ArVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsmottakerIkkeFylt6ArVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()

data class OmsorgsmottakerIkkeFylt6ArInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsmottakerIkkeFylt6ArAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()

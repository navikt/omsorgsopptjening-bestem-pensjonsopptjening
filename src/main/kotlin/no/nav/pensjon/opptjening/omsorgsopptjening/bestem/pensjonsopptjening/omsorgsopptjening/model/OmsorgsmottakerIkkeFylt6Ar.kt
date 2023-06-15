package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.GrunnlagVilkårsvurderingDb

class OmsorgsmottakerIkkeFylt6Ar : Vilkar<PersonOgOmsorgsårGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Omsorgsmottaker kan ikke ha fylt 6 år i omsorgsåret",
        begrunnesleForAvslag = "Omsorgsmottaker har fylt 6 år i omsorgsåret",
        begrunnelseForInnvilgelse = "Omsorgsmottaker har ikke fylt fylt 6 år i omsorgsåret",
    ),
    utfallsFunksjon = vurderUtfall,
) {

    companion object {
        private val vurderUtfall =
            fun Vilkar<PersonOgOmsorgsårGrunnlag>.(input: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
                if(input.alderMottaker(mellom = 0..5)) {
                    return OmsorgsmottakerIkkeFylt6ArInnvilget("")
                } else {
                    return OmsorgsmottakerIkkeFylt6ArAvslag(listOf(AvslagÅrsak.BARN_IKKE_MELLOM_1_OG_5))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsmottakerIkkeFylt6ArVurdering {
        return OmsorgsmottakerIkkeFylt6ArVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class OmsorgsmottakerIkkeFylt6ArVurdering(
    override val vilkar: Vilkar<PersonOgOmsorgsårGrunnlag>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<PersonOgOmsorgsårGrunnlag>()

data class OmsorgsmottakerIkkeFylt6ArInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class OmsorgsmottakerIkkeFylt6ArAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()

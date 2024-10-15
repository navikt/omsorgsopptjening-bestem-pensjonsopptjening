package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsyterErikkeOmsorgsmottaker : ParagrafVilkår<OmsorgsyterErikkeOmsorgsmottaker.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag.omsorgsyter == grunnlag.omsorgsmottaker) {
            true -> VilkårsvurderingUtfall.Avslag.Vilkår(emptySet())
            false -> VilkårsvurderingUtfall.Innvilget.Vilkår(emptySet())
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
    ) : ParagrafGrunnlag()
}
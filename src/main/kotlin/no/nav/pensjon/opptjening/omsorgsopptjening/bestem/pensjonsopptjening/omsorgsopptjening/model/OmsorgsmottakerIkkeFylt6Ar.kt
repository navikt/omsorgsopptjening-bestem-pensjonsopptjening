package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsmottakerIkkeFylt6Ar : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsmottakerErIkkeFylt6FørUtgangAvOpptjeningsår()
        ).let {
            if (grunnlag.alderMottaker(mellom = 0..5)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val henvisninger: Set<Henvisning>,
        override val grunnlag: PersonOgOmsorgsårGrunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
}

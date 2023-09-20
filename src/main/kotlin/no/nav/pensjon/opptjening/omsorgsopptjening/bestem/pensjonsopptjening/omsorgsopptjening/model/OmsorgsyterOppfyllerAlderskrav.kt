package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsyterOppfyllerAlderskrav : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller17
        ).let {
            if (grunnlag.alderMottaker(Konstanter.ALDERSINTERVALL_OMSORGSYTER)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: PersonOgOmsorgsårGrunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
}
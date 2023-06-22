package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller69()
        ).let {
            if (grunnlag.person.alderVedUtløpAv(grunnlag.omsorgsAr) <= 69) {
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
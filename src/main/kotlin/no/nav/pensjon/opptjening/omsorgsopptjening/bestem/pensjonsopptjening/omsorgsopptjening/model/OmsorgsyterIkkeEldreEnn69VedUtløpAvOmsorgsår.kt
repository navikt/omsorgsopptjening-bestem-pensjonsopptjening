package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering {
        return bestemUtfall(grunnlag).let { OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
            henvisninger = it.henvisninger(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
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
}

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
    override val henvisninger: Set<Henvisning>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
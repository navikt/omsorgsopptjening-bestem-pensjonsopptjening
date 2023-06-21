package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterFylt17VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterFylt17ÅrVurdering {
        return bestemUtfall(grunnlag).let { OmsorgsyterFylt17ÅrVurdering(
            henvisninger = it.henvisninger(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningKanGodskrivesFraOgMedÅretManFyller17()
        ).let {
            if (grunnlag.person.alderVedUtløpAv(grunnlag.omsorgsAr) >= 17) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }
}

data class OmsorgsyterFylt17ÅrVurdering(
    override val henvisninger: Set<Henvisning>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
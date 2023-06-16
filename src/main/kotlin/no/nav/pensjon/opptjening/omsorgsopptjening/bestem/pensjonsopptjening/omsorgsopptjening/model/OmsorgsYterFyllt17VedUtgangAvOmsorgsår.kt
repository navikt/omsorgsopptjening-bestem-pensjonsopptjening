package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterFylt17VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterFylt17ÅrVurdering {
        return bestemUtfall(grunnlag).let { OmsorgsyterFylt17ÅrVurdering(
            lovhenvisninger = it.lovhenvisning(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        val lovhenvisning = setOf(Lovhenvisning.FYLLER_17_AR)
        return if (grunnlag.person.alderVedUtløpAv(grunnlag.omsorgsAr) >= 17) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
        }
    }
}

data class OmsorgsyterFylt17ÅrVurdering(
    override val lovhenvisninger: Set<Lovhenvisning>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
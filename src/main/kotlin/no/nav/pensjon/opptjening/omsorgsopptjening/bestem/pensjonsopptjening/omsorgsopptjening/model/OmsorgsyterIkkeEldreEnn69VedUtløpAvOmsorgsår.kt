package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>() {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering {
        return bestemUtfall(grunnlag).let { OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
            lovhenvisninger = it.lovhenvisning(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        val lovhenvisning = setOf(Lovhenvisning.FYLLER_69_AR)
        return if (grunnlag.person.alderVedUtløpAv(grunnlag.omsorgsAr) <= 69) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning)
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning)
        }
    }
}

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
    override val lovhenvisninger: Set<Lovhenvisning>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
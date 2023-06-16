package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>(
    paragrafer = setOf(Paragraf.A),
) {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering {
        return OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return if (grunnlag.person.alderVedUtløpAv(grunnlag.omsorgsAr) <= 69) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(paragrafer.single())
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(paragrafer.single())
        }
    }
}

data class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
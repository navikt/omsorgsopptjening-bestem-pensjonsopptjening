package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsyterFylt17VedUtløpAvOmsorgsår : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>(
    paragrafer = setOf(Paragraf.A),
) {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsyterFylt17ÅrVurdering {
        return OmsorgsyterFylt17ÅrVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return if (grunnlag.person.alderVedUtløpAv(grunnlag.omsorgsAr) >= 17) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(paragraf = paragrafer.single())
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(paragraf = paragrafer.single())
        }
    }
}

data class OmsorgsyterFylt17ÅrVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()
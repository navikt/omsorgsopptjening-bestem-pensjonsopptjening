package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class OmsorgsmottakerIkkeFylt6Ar : ParagrafVilkår<PersonOgOmsorgsårGrunnlag>(
    paragrafer = setOf(Paragraf.A),
) {
    override fun vilkarsVurder(grunnlag: PersonOgOmsorgsårGrunnlag): OmsorgsmottakerIkkeFylt6ArVurdering {
        return OmsorgsmottakerIkkeFylt6ArVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<PersonOgOmsorgsårGrunnlag>> T.bestemUtfall(grunnlag: PersonOgOmsorgsårGrunnlag): VilkårsvurderingUtfall {
        return if (grunnlag.alderMottaker(mellom = 0..5)) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(paragraf = paragrafer.single())
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(paragraf = paragrafer.single())
        }
    }
}

data class OmsorgsmottakerIkkeFylt6ArVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: PersonOgOmsorgsårGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<PersonOgOmsorgsårGrunnlag>()

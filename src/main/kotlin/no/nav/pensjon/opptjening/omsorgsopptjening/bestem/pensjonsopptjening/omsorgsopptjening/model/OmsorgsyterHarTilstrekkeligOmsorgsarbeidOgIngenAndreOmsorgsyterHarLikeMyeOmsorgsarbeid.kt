package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

object OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid :
    ParagrafVilkår<OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder
        ).let { referanse ->
            og(
                grunnlag.fullOmsorgForBarnUnder6Vurdering,
                grunnlag.liktAntallMånederOmsorgVurdering
            ).let {
                if (it.utfall.erInnvilget()) {
                    VilkårsvurderingUtfall.Innvilget.Vilkår.from(referanse)
                } else {
                    VilkårsvurderingUtfall.Avslag.Vilkår.from(referanse)
                }
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val fullOmsorgForBarnUnder6Vurdering: OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering,
        val liktAntallMånederOmsorgVurdering: OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Vurdering,
    ) : ParagrafGrunnlag()
}
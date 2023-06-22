package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

class FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder :
    ParagrafVilkår<FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag>() {
    override fun vilkarsVurder(grunnlag: FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag): FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering {
        return bestemUtfall(grunnlag).let {
            FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering(
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag>> T.bestemUtfall(grunnlag: FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder()
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
}

data class FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering(
    override val henvisninger: Set<Henvisning>,
    override val grunnlag: FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag>()

data class FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederGrunnlag(
    val fullOmsorgForBarnUnder6Vurdering: FullOmsorgForBarnUnder6Vurdering,
    val liktAntallMånederOmsorgVurdering: LiktAntallMånederOmsorgVurdering,
) : ParagrafGrunnlag()
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.PersonMedFødselsår

object OmsorgstyerHarMestOmsorgAvAlleOmsorgsytere :
    ParagrafVilkår<OmsorgstyerHarMestOmsorgAvAlleOmsorgsytere.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder //TODO rett opp henvinsinger/presiser?
        ).let {
            if (grunnlag.omsorgsyterHarFlest()) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsyter: PersonMedFødselsår,
        val summert: List<SummertOmsorgForMottakerOgÅr>
    ) : ParagrafGrunnlag() {
        val yterTilAntall = summert.associate { it.omsorgsyter.fnr to it.antallMåneder }
        fun omsorgsyterHarFlest(): Boolean {
            return yterTilAntall.filterNot { it.key == omsorgsyter.fnr }.none { it.value >= yterTilAntall[omsorgsyter.fnr]!! }
            }

        fun flereHarLikeMange(): Boolean {
            return yterTilAntall.count() > 1 && yterTilAntall.map { it.value }.distinct().count() == 1
        }
    }


    data class SummertOmsorgForMottakerOgÅr(
        val omsorgsyter: PersonMedFødselsår,
        val omsorgsmottaker: PersonMedFødselsår,
        val antallMåneder: Int,
        val år: Int
    )
}
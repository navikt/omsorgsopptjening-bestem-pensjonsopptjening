package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.time.YearMonth

object OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere :
    ParagrafVilkår<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Grunnlag>() {
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
        val omsorgsyter: Person,
        val summert: List<OmsorgsmånederForMottakerOgÅr>
    ) : ParagrafGrunnlag() {
        val yterTilAntall = summert.associate { it.omsorgsyter.fnr to it.antall() }

        private fun andreOmsorgsytere(): Map<String, Int> {
            return yterTilAntall.filterNot { it.key == omsorgsyter.fnr }
        }

        fun andreOmsorgsytereMedLikeMange(): Map<String, Int> {
            return andreOmsorgsytere().filter { it.value == yterTilAntall[omsorgsyter.fnr]!! }
        }

        fun omsorgsyterHarFlest(): Boolean {
            return andreOmsorgsytere().none { it.value >= yterTilAntall[omsorgsyter.fnr]!! }
        }

        fun flereHarLikeMange(): Boolean {
            return andreOmsorgsytereMedLikeMange().isNotEmpty()
        }
    }


    data class OmsorgsmånederForMottakerOgÅr(
        val omsorgsyter: Person,
        val omsorgsmottaker: Person,
        val omsorgsmåneder: Set<YearMonth>,
        val år: Int
    ) {
        fun antall(): Int {
            return omsorgsmåneder.count()
        }
    }
}
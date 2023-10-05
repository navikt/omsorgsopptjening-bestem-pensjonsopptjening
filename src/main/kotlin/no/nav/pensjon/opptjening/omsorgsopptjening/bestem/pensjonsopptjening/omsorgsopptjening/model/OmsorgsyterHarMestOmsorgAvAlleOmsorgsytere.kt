package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsmåneder
import java.time.Month
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
            Referanse.OmsorgsopptjeningGisHvisOmsorgsyterHarFlestManeder //TODO rett opp henvinsinger/presiser hvorfor det er slik? tolkning/forskrift/lov whatever
        ).let {
            if (grunnlag.omsorgsyterHarFlestOmsorgsmåneder()) {
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
        val omsorgsyter: String,
        val data: List<OmsorgsmånederForMottakerOgÅr> //TODO stramme opp modellen slik at det ikke er teoretisk mulig med flere mottakere?
    ) : ParagrafGrunnlag() {
        private val omsorgsytereGruppertEtterOmsorgsmåneder = data
            .groupBy { it.antall() }
        private val omsorgsytereMedFlestOmsorgsmåneder = omsorgsytereGruppertEtterOmsorgsmåneder
            .let { map -> map[map.maxOf { it.key }]!! }

        fun omsorgsyterHarFlestOmsorgsmåneder(): Boolean {
            return omsorgsytereMedFlestOmsorgsmåneder().let { omsorgsytere ->
                omsorgsytere.count() == 1 && omsorgsytere.all { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder(): Boolean {
            return omsorgsytereMedFlestOmsorgsmåneder().let { omsorgsytere ->
                omsorgsytere.count() > 1 && omsorgsytere.any { it.omsorgsyter == omsorgsyter }
            }
        }

        fun omsorgsytereMedFlestOmsorgsmåneder(): Set<OmsorgsmånederForMottakerOgÅr> {
            return omsorgsytereMedFlestOmsorgsmåneder.toSet()
        }
    }


    data class OmsorgsmånederForMottakerOgÅr(
        val omsorgsyter: String,
        val omsorgsmottaker: String,
        val omsorgsmåneder: Omsorgsmåneder,
        val omsorgsår: Int
    ) {
        fun antall(): Int {
            return omsorgsmåneder.count()
        }

        fun haddeOmsorgIDesember(): Boolean {
            return omsorgsmåneder.contains(YearMonth.of(omsorgsår, Month.DECEMBER))
        }
    }
}
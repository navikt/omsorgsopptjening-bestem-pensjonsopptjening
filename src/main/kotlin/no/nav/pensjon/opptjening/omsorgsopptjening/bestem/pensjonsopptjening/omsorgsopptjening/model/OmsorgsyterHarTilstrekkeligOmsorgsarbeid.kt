package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import java.time.Month

/**
 * For barn fra 1 til og med 5 år må omsorgsyter minst ha 6 måneder med omsorgsarbeid for barnet
 *
 * For barn som ikke har fylt ett år kreves ikke 6 måneder for å oppnå omsorgsopptjening
 *
 * Barn som ikke har fylt ett år og er født i desember vil ikke ha utbetalt barnetrygd og har ikke omsorgsarbeid for året.
 * De har alikevel rett til full omsorgsopptjening det første året.
 * Det betyr at vi må sjekke om omsorgsyter har fått barnetrygd i året etter for å vite om omsorgsyter har rett til omsorgsopptjening
 *
 */
object OmsorgsyterHarTilstrekkeligOmsorgsarbeid : ParagrafVilkår<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag) {
            is Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår(),
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd()
                ).let {
                    if (grunnlag.minstEnMånedFullOmsorg) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            is Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
                setOf(
                    Referanse.MåHaMinstHalveÅretMedOmsorg(),
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd()
                ).let {
                    if (grunnlag.minstSeksMånederFullOmsorg) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            is Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår(),
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd()
                ).let {
                    if (grunnlag.minstEnMånedOmsorgÅretEtterFødsel) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()


    sealed class Grunnlag : ParagrafGrunnlag() {
        abstract val omsorgsAr: Int
        abstract val omsorgsmottaker: PersonMedFødselsår

        data class OmsorgsmottakerFødtUtenforOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsår,
            val minstSeksMånederFullOmsorg: Boolean,
        ) : Grunnlag() {
            init {
                require(!omsorgsmottaker.erFødt(omsorgsAr))
            }
        }

        data class OmsorgsmottakerFødtIOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsår,
            val minstEnMånedFullOmsorg: Boolean,
        ) : Grunnlag() {
            init {
                require(omsorgsmottaker.erFødt(omsorgsAr))
            }
        }

        data class OmsorgsmottakerFødtIDesemberOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: PersonMedFødselsår,
            val minstEnMånedOmsorgÅretEtterFødsel: Boolean,
        ) : Grunnlag() {
            init {
                require(omsorgsmottaker.erFødt(omsorgsAr, Month.DECEMBER))
            }
        }
    }
}


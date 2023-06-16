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
class FullOmsorgForBarnUnder6 : ParagrafVilkår<FullOmsorgForBarnUnder6Grunnlag>() {
    override fun vilkarsVurder(grunnlag: FullOmsorgForBarnUnder6Grunnlag): FullOmsorgForBarnUnder6Vurdering {
        return bestemUtfall(grunnlag).let {
            FullOmsorgForBarnUnder6Vurdering(
                lovhenvisninger = it.lovhenvisning(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<FullOmsorgForBarnUnder6Grunnlag>> T.bestemUtfall(grunnlag: FullOmsorgForBarnUnder6Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag) {
            is OmsorgsmottakerFødtIOmsorgsårGrunnlag -> {
                val lovhenvisning = setOf(Lovhenvisning.IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR, Lovhenvisning.OPPTJENING_GIS_BARNETRYGDMOTTAKER)
                if (grunnlag.minstEnMånedFullOmsorg) {
                    VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
                } else {
                    VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
                }
            }

            is OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag -> {
                val lovhenvisning = setOf(Lovhenvisning.MINST_HALVT_AR_OMSORG, Lovhenvisning.OPPTJENING_GIS_BARNETRYGDMOTTAKER)
                if (grunnlag.minstSeksMånederFullOmsorg) {
                    VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
                } else {
                    VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
                }
            }

            is OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag -> {
                val lovhenvisning = setOf(Lovhenvisning.IKKE_KRAV_OM_MINST_HALVT_AR_I_FODSELSAR, Lovhenvisning.OPPTJENING_GIS_BARNETRYGDMOTTAKER)
                if (grunnlag.minstEnMånedOmsorgÅretEtterFødsel) {
                    VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
                } else {
                    VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
                }
            }
        }
    }
}

data class FullOmsorgForBarnUnder6Vurdering(
    override val lovhenvisninger: Set<Lovhenvisning>,
    override val grunnlag: FullOmsorgForBarnUnder6Grunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<FullOmsorgForBarnUnder6Grunnlag>()

sealed class FullOmsorgForBarnUnder6Grunnlag : ParagrafGrunnlag() {
    abstract val omsorgsAr: Int
    abstract val omsorgsmottaker: PersonMedFødselsår
}

data class OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag(
    override val omsorgsAr: Int,
    override val omsorgsmottaker: PersonMedFødselsår,
    val minstSeksMånederFullOmsorg: Boolean,
) : FullOmsorgForBarnUnder6Grunnlag() {
    init {
        require(!omsorgsmottaker.erFødt(omsorgsAr))
    }
}

data class OmsorgsmottakerFødtIOmsorgsårGrunnlag(
    override val omsorgsAr: Int,
    override val omsorgsmottaker: PersonMedFødselsår,
    val minstEnMånedFullOmsorg: Boolean,
) : FullOmsorgForBarnUnder6Grunnlag() {
    init {
        require(omsorgsmottaker.erFødt(omsorgsAr))
    }
}

data class OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag(
    override val omsorgsAr: Int,
    override val omsorgsmottaker: PersonMedFødselsår,
    val minstEnMånedOmsorgÅretEtterFødsel: Boolean,
) : FullOmsorgForBarnUnder6Grunnlag() {
    init {
        require(omsorgsmottaker.erFødt(omsorgsAr, Month.DECEMBER))
    }
}

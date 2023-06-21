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
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<FullOmsorgForBarnUnder6Grunnlag>> T.bestemUtfall(grunnlag: FullOmsorgForBarnUnder6Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag) {
            is OmsorgsmottakerFødtIOmsorgsårGrunnlag -> {
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

            is OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag -> {
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

            is OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag -> {
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
}

data class FullOmsorgForBarnUnder6Vurdering(
    override val henvisninger: Set<Henvisning>,
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

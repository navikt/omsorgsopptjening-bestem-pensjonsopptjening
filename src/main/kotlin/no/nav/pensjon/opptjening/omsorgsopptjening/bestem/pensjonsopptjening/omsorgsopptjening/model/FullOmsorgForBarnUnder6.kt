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
class FullOmsorgForBarnUnder6 : ParagrafVilkår<FullOmsorgForBarnUnder6Grunnlag>(
    paragrafer = setOf(Paragraf.A),
    utfallsFunksjon = vurderUtfall as Vilkar<FullOmsorgForBarnUnder6Grunnlag>.(FullOmsorgForBarnUnder6Grunnlag) -> VilkårsvurderingUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun ParagrafVilkår<FullOmsorgForBarnUnder6Grunnlag>.(grunnlag: FullOmsorgForBarnUnder6Grunnlag): VilkårsvurderingUtfall {
                return this.let { vilkar ->
                    when (grunnlag) {
                        is OmsorgsmottakerFødtIOmsorgsårGrunnlag -> {
                            if (grunnlag.minstEnMånedFullOmsorg) {
                                FullOmsorgForBarnUnder6Innvilget(
                                    årsak = "",
                                    omsorgsmottaker = grunnlag.omsorgsmottaker
                                )
                            } else {
                                FullOmsorgForBarnUnder6Avslag(
                                    årsaker = mutableListOf<AvslagÅrsak>().let {
                                        if (!grunnlag.minstEnMånedFullOmsorg) it.add(AvslagÅrsak.INGEN_MÅNEDER_FULL_OMSORG)
                                        it.toList()
                                    }
                                )
                            }
                        }

                        is OmsorgsmottakerFødtUtenforOmsorgsårGrunnlag -> {
                            if (grunnlag.minstSeksMånederFullOmsorg) {
                                FullOmsorgForBarnUnder6Innvilget(
                                    årsak = "",
                                    omsorgsmottaker = grunnlag.omsorgsmottaker
                                )
                            } else {
                                FullOmsorgForBarnUnder6Avslag(
                                    årsaker = mutableListOf<AvslagÅrsak>().let {
                                        if (!grunnlag.minstSeksMånederFullOmsorg) it.add(AvslagÅrsak.MINDRE_ENN_6_MND_FULL_OMSORG)
                                        it.toList()
                                    }
                                )
                            }
                        }

                        is OmsorgsmottakerFødtIDesemberOmsorgsårGrunnlag -> {
                            if (grunnlag.minstEnMånedOmsorgÅretEtterFødsel) {
                                FullOmsorgForBarnUnder6Innvilget(
                                    årsak = "",
                                    omsorgsmottaker = grunnlag.omsorgsmottaker
                                )
                            } else {
                                FullOmsorgForBarnUnder6Avslag(
                                    årsaker = mutableListOf<AvslagÅrsak>().let {
                                        if (!grunnlag.minstEnMånedOmsorgÅretEtterFødsel) it.add(AvslagÅrsak.INGEN_MÅNEDER_FULL_OMSORG_ÅR_ETTER_FØDSEL)
                                        it.toList()
                                    }
                                )
                            }
                        }
                    }
                }
            }
    }

    override fun vilkarsVurder(grunnlag: FullOmsorgForBarnUnder6Grunnlag): FullOmsorgForBarnUnder6Vurdering {
        return FullOmsorgForBarnUnder6Vurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class FullOmsorgForBarnUnder6Vurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: FullOmsorgForBarnUnder6Grunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<FullOmsorgForBarnUnder6Grunnlag>()


data class FullOmsorgForBarnUnder6Innvilget(
    val årsak: String,
    val omsorgsmottaker: PersonMedFødselsår
) : VilkårsvurderingUtfall.Innvilget()

data class FullOmsorgForBarnUnder6Avslag(
    override val årsaker: List<AvslagÅrsak>,
) : VilkårsvurderingUtfall.Avslag()

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
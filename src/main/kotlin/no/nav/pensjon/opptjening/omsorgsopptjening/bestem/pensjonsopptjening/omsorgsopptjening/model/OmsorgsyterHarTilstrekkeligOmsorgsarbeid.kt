package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Omsorgsmåneder
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
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag) {
            is Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd
                ).let {
                    if (grunnlag.antallMåneder() >= 1) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            is Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> {
                when (grunnlag.omsorgstype()) {
                    DomainOmsorgstype.BARNETRYGD -> {
                        setOf(
                            Referanse.MåHaMinstHalveÅretMedOmsorgForBarnUnder6,
                            Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd
                        )
                    }

                    DomainOmsorgstype.HJELPESTØNAD -> {
                        setOf(
                            Referanse.MåHaMinstHalveÅretMedOmsorgForSykFunksjonshemmetEllerEldre,
                            Referanse.OmsorgsopptjeningGisTilForelderSomMottarBarnetrygdForBarnMedForhøyetHjelpestønad
                        )
                    }
                }.let {
                    if (grunnlag.antallMåneder() >= 6) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            is Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd
                ).let {
                    if (grunnlag.antallMåneder() >= 1) {
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
    ) : ParagrafVurdering<Grunnlag>() {
        fun omsorgstype(): DomainOmsorgstype {
            return grunnlag.omsorgstype()
        }
    }


    sealed class Grunnlag : ParagrafGrunnlag() {
        abstract val omsorgsAr: Int
        abstract val omsorgsmottaker: Person
        abstract val omsorgsmåneder: Omsorgsmåneder
        fun antallMåneder(): Int {
            return omsorgsmåneder.count()
        }

        fun omsorgstype(): DomainOmsorgstype {
            return when (omsorgsmåneder) {
                is Omsorgsmåneder.Barnetrygd -> DomainOmsorgstype.BARNETRYGD
                is Omsorgsmåneder.Hjelpestønad -> DomainOmsorgstype.HJELPESTØNAD
            }
        }

        data class OmsorgsmottakerFødtUtenforOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: Person,
            override val omsorgsmåneder: Omsorgsmåneder,
        ) : Grunnlag() {
            init {
                require(!omsorgsmottaker.erFødt(omsorgsAr)) { "Ugyldig data. Omsorgsmottaker: $omsorgsmottaker er født i omsorgsår: $omsorgsAr" }
            }
        }

        data class OmsorgsmottakerFødtIOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: Person,
            override val omsorgsmåneder: Omsorgsmåneder,
        ) : Grunnlag() {
            init {
                require(omsorgsmottaker.erFødt(omsorgsAr)) { "Ugyldig data. Omsorgsmottaker: $omsorgsmottaker er ikke født i omsorgsår: $omsorgsAr" }
            }
        }

        data class OmsorgsmottakerFødtIDesemberOmsorgsår(
            override val omsorgsAr: Int,
            override val omsorgsmottaker: Person,
            override val omsorgsmåneder: Omsorgsmåneder,
        ) : Grunnlag() {
            init {
                require(omsorgsmottaker.erFødt(omsorgsAr, Month.DECEMBER)) { "Ugyldig data. Omsorgsmottaker: $omsorgsmottaker er ikke født desember i omsorgsår: $omsorgsAr" }
            }
        }
    }
}


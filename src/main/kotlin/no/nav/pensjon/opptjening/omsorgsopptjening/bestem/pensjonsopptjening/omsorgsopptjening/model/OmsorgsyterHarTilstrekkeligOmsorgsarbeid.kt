package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Omsorgsmåneder

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
            påkrevetAntallMåneder = grunnlag.påkrevetAntallMåneder(),
        )
    }

    fun Grunnlag.påkrevetAntallMåneder(): Int {
        return when (this) {
            is Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår -> 1
            is Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> 1
            is Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår -> 6
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return when (grunnlag) {
            is Grunnlag.OmsorgsmottakerFødtIOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd
                ).let {
                    if (grunnlag.erOppfylltFor(grunnlag.påkrevetAntallMåneder())) {
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
                    if (grunnlag.erOppfylltFor(grunnlag.påkrevetAntallMåneder())) {
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
                    if (grunnlag.erOppfylltFor(grunnlag.påkrevetAntallMåneder())) {
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
        override val utfall: VilkårsvurderingUtfall,
        val påkrevetAntallMåneder: Int
    ) : ParagrafVurdering<Grunnlag>() {
        fun omsorgstype(): DomainOmsorgstype {
            return grunnlag.omsorgstype()
        }
    }


    sealed class Grunnlag : ParagrafGrunnlag() {
        abstract val aldersvurderingOmsorgsmottaker: AldersvurderingsGrunnlag
        abstract val omsorgsytersOmsorgsmånederForOmsorgsmottaker: Omsorgsmåneder

        fun erOppfylltFor(påkrevetAntallMåneder: Int): Boolean {
            return omsorgsytersOmsorgsmånederForOmsorgsmottaker.count() >= påkrevetAntallMåneder
        }

        fun omsorgstype(): DomainOmsorgstype {
            return when (omsorgsytersOmsorgsmånederForOmsorgsmottaker) {
                is Omsorgsmåneder.Barnetrygd -> DomainOmsorgstype.BARNETRYGD
                is Omsorgsmåneder.Hjelpestønad -> DomainOmsorgstype.HJELPESTØNAD
            }
        }

        data class OmsorgsmottakerFødtUtenforOmsorgsår(
            override val aldersvurderingOmsorgsmottaker: AldersvurderingsGrunnlag,
            override val omsorgsytersOmsorgsmånederForOmsorgsmottaker: Omsorgsmåneder,
        ) : Grunnlag() {
            init {
                require(aldersvurderingOmsorgsmottaker.fødselsår() != aldersvurderingOmsorgsmottaker.omsorgsAr) { "Ugyldig data. Omsorgsmottaker: ${aldersvurderingOmsorgsmottaker.person} er født i omsorgsår: ${aldersvurderingOmsorgsmottaker.omsorgsAr}" }
            }
        }

        data class OmsorgsmottakerFødtIOmsorgsår(
            override val aldersvurderingOmsorgsmottaker: AldersvurderingsGrunnlag,
            override val omsorgsytersOmsorgsmånederForOmsorgsmottaker: Omsorgsmåneder,
        ) : Grunnlag() {
            init {
                require(aldersvurderingOmsorgsmottaker.fødselsår() == aldersvurderingOmsorgsmottaker.omsorgsAr) { "Ugyldig data. Omsorgsmottaker: ${aldersvurderingOmsorgsmottaker.person} er ikke født i omsorgsår: ${aldersvurderingOmsorgsmottaker.omsorgsAr}" }
            }
        }

        data class OmsorgsmottakerFødtIDesemberOmsorgsår(
            override val aldersvurderingOmsorgsmottaker: AldersvurderingsGrunnlag,
            override val omsorgsytersOmsorgsmånederForOmsorgsmottaker: Omsorgsmåneder,
        ) : Grunnlag() {
            init {
                require(aldersvurderingOmsorgsmottaker.fødselsår() == aldersvurderingOmsorgsmottaker.omsorgsAr) { "Ugyldig data. Omsorgsmottaker: ${aldersvurderingOmsorgsmottaker.person} er ikke født desember i omsorgsår: ${aldersvurderingOmsorgsmottaker.omsorgsAr}" }
            }
        }
    }
}


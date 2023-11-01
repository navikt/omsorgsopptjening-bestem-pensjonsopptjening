package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype

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
        return when (grunnlag.antallMånederRegel) {
            AntallMånederRegel.FødtIOmsorgsår -> {
                setOf(
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd
                ).let {
                    if (grunnlag.erOppfyllt()) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            AntallMånederRegel.FødtUtenforOmsorgsår -> {
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
                    if (grunnlag.erOppfyllt()) {
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


    data class Grunnlag(
        val omsorgsytersOmsorgsmånederForOmsorgsmottaker: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel
    ) : ParagrafGrunnlag() {

        fun erOppfyllt(): Boolean {
            return omsorgsytersOmsorgsmånederForOmsorgsmottaker.alleMåneder().count() >= antallMånederRegel.antall
        }

        fun omsorgstype(): DomainOmsorgstype {
            return when (omsorgsytersOmsorgsmånederForOmsorgsmottaker) {
                is Omsorgsmåneder.Barnetrygd -> DomainOmsorgstype.BARNETRYGD
                is Omsorgsmåneder.Hjelpestønad -> DomainOmsorgstype.HJELPESTØNAD
            }
        }
    }
}


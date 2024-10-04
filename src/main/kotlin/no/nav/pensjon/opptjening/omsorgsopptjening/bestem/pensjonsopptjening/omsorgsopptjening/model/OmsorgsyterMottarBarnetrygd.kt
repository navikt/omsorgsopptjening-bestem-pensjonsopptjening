package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori


object OmsorgsyterMottarBarnetrgyd : ParagrafVilkår<OmsorgsyterMottarBarnetrgyd.Grunnlag>() {
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
                    Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd,
                ).let {
                    if (grunnlag.erOppfyllt()) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            AntallMånederRegel.FødtUtenforOmsorgsår -> {
                when (grunnlag.omsorgstype) {
                    DomainOmsorgskategori.BARNETRYGD -> {
                        setOf(
                            Referanse.OmsorgsopptjeningGisTilMottakerAvBarnetrygd
                        )
                    }

                    DomainOmsorgskategori.HJELPESTØNAD -> {
                        setOf(
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
        val omsorgsytersUtbetalingsmåneder: Utbetalingsmåneder,
        val antallMånederRegel: AntallMånederRegel,
        val omsorgstype: DomainOmsorgskategori,
    ) : ParagrafGrunnlag() {
        fun erOppfyllt(): Boolean {
            return omsorgsytersUtbetalingsmåneder.antall() >= antallMånederRegel.antall
        }
    }
}



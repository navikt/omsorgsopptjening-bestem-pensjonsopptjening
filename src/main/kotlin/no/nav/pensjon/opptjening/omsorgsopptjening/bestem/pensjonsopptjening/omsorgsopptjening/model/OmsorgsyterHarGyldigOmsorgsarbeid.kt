package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import java.time.YearMonth


object OmsorgsyterHarGyldigOmsorgsarbeid : ParagrafVilkår<OmsorgsyterHarGyldigOmsorgsarbeid.Grunnlag>() {
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
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Andre_Punktum,
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum
                ).let {
                    when {
                        grunnlag.erOppfyllt() -> {
                            VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                        }

                        else -> {
                            VilkårsvurderingUtfall.Avslag.Vilkår(it)
                        }
                    }
                }
            }

            AntallMånederRegel.FødtUtenforOmsorgsår -> {
                when (grunnlag.omsorgstype()) {
                    DomainOmsorgskategori.BARNETRYGD -> {
                        setOf(
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum,
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Tredje_Punktum
                        )
                    }

                    DomainOmsorgskategori.HJELPESTØNAD -> {
                        setOf(
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum,
                            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_11_Andre_Ledd_Første_Punktum
                        )
                    }
                }.let {
                    when {
                        grunnlag.erOppfyllt() -> {
                            VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                        }

                        else -> {
                            VilkårsvurderingUtfall.Avslag.Vilkår(it)
                        }
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
        val omsorgsytersOmsorgsmåneder: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {
        val gyldigeOmsorgsmåneder: Set<YearMonth> =
            omsorgsytersOmsorgsmåneder.alle().intersect(omsorgsytersUtbetalingsmåneder.alle())

        fun erOppfyllt(): Boolean {
            return gyldigeOmsorgsmåneder.count() >= antallMånederRegel.antall
        }

        fun omsorgstype(): DomainOmsorgskategori {
            return omsorgsytersOmsorgsmåneder.omsorgstype()
        }
    }
}
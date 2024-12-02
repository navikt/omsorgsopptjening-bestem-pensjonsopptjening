package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori

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
                    JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Andre_Punktum,
                ).let {
                    when {
                        grunnlag.erOppfyllt() -> {
                            VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                        }

                        grunnlag.erManuell() -> {
                            VilkårsvurderingUtfall.Ubestemt.Vilkår(it)
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
                            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
                        )
                    }

                    DomainOmsorgskategori.HJELPESTØNAD -> {
                        setOf(
                            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Andre_Ledd,
                            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_b_Første_Punktum
                        )
                    }
                }.let {
                    when {
                        grunnlag.erOppfyllt() -> {
                            VilkårsvurderingUtfall.Innvilget.Vilkår(it)
                        }

                        grunnlag.erManuell() -> {
                            VilkårsvurderingUtfall.Ubestemt.Vilkår(it)
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
    ) : ParagrafVurdering<Grunnlag>() {
        override fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger {
            return Oppgaveopplysninger.Generell(
                oppgavemottaker = behandling.omsorgsyter,
                oppgaveTekst = Oppgave.kombinasjonAvFullOgDeltErTilstrekkelig(behandling.omsorgsmottaker)
            )
        }
    }


    data class Grunnlag private constructor(
        val omsorgsmåneder: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {

        companion object {
            fun new(omsorgsmåneder: Omsorgsmåneder, antallMånederRegel: AntallMånederRegel): Grunnlag {
                return Grunnlag(
                    omsorgsmåneder = if (omsorgsmåneder.erKvalifisertForAutomatiskBehandling(antallMånederRegel)) {
                        omsorgsmåneder.kvalifisererForAutomatiskBehandling()
                    } else {
                        omsorgsmåneder.kvalifisererForManuellBehandling()
                    },
                    antallMånederRegel = antallMånederRegel
                )
            }

            fun persistent(omsorgsmåneder: Omsorgsmåneder, antallMånederRegel: AntallMånederRegel): Grunnlag {
                return Grunnlag(
                    omsorgsmåneder = omsorgsmåneder,
                    antallMånederRegel = antallMånederRegel
                )
            }
        }

        fun erOppfyllt(): Boolean {
            return omsorgsmåneder.erKvalifisertForAutomatiskBehandling(antallMånederRegel)
        }

        fun erManuell(): Boolean {
            require(!erOppfyllt()) { "Rekkefølgeavhengig" }
            return omsorgsmåneder.erKvalifisertForManuellBehandling(antallMånederRegel)
        }

        fun omsorgstype(): DomainOmsorgskategori {
            return omsorgsmåneder.omsorgstype()
        }

        fun omsorgsmåneder(): Omsorgsmåneder {
            return omsorgsmåneder
        }
    }
}
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
                            VilkårsvurderingUtfall.Ubestemt(it)
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
                            VilkårsvurderingUtfall.Ubestemt(it)
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


    data class Grunnlag(
        val omsorgsytersOmsorgsmånederForOmsorgsmottaker: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {

        fun erOppfyllt(): Boolean {
            return when (omsorgsytersOmsorgsmånederForOmsorgsmottaker) {
                is Omsorgsmåneder.Barnetrygd -> {
                    omsorgsytersOmsorgsmånederForOmsorgsmottaker.antallFull().oppfyller(antallMånederRegel)
                }

                is Omsorgsmåneder.Hjelpestønad -> {
                    //TODO et hull her ift full vs delt barnetrygd
                    omsorgsytersOmsorgsmånederForOmsorgsmottaker.antall().oppfyller(antallMånederRegel)
                }
            }
        }

        fun erManuell(): Boolean {
            require(!erOppfyllt()) { "Rekkefølgeavhengig" }
            return when (omsorgsytersOmsorgsmånederForOmsorgsmottaker) {
                is Omsorgsmåneder.Barnetrygd -> {
                    (omsorgsytersOmsorgsmånederForOmsorgsmottaker.antallFull() + omsorgsytersOmsorgsmånederForOmsorgsmottaker.antallDelt()).oppfyller(antallMånederRegel)
                }

                is Omsorgsmåneder.Hjelpestønad -> {
                    //TODO et hull her ift full vs delt barnetrygd
                    false
                }
            }
        }

        fun omsorgstype(): DomainOmsorgskategori {
            return omsorgsytersOmsorgsmånederForOmsorgsmottaker.omsorgstype()
        }
    }
}
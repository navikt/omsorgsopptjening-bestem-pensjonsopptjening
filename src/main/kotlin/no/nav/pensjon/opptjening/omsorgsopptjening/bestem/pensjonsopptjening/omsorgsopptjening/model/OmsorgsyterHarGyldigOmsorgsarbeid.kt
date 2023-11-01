package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


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
                    Referanse.UnntakFraMinstHalvtÅrMedOmsorgForFødselår,
                ).let {
                    if (grunnlag.erOppfyllt()) {
                        VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
                    } else {
                        VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
                    }
                }
            }

            AntallMånederRegel.FødtUtenforOmsorgsår -> {
                setOf(
                    Referanse.MåHaMinstHalveÅretMedOmsorgForBarnUnder6,
                ).let {
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
        val omsorgsytersMedlemskapsmåneder: Medlemskapsmåneder,
        val omsorgsytersUtbetalingsmåneder: Utbetalingsmåneder,
        val omsorgsytersOmsorgsmåneder: Omsorgsmåneder,
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {
        val gyldigeOmsorgsmåneder: GyldigeOmsorgsmåneder
            get() = GyldigeOmsorgsmåneder.of(
                omsorgsmåneder = omsorgsytersOmsorgsmåneder,
                medlemskapsmåneder = omsorgsytersMedlemskapsmåneder,
                utbetalingsmåneder = omsorgsytersUtbetalingsmåneder
            )

        fun erOppfyllt(): Boolean {
            return gyldigeOmsorgsmåneder.alleMåneder().count() >= antallMånederRegel.antall
        }
    }
}


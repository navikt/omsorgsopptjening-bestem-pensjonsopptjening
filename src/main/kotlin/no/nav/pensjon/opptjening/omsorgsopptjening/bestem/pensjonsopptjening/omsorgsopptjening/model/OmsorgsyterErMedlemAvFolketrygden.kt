package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


object OmsorgsyterErMedlemAvFolketrygden : ParagrafVilkår<OmsorgsyterErMedlemAvFolketrygden.Grunnlag>() {
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
        val antallMånederRegel: AntallMånederRegel,
    ) : ParagrafGrunnlag() {

        fun erOppfyllt(): Boolean {
            return omsorgsytersMedlemskapsmåneder.alleMåneder().count() >= antallMånederRegel.antall
        }
    }
}
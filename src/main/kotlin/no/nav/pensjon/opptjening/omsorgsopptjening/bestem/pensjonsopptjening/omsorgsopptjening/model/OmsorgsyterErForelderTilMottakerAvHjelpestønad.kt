package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsyterErForelderTilMottakerAvHjelpestønad :
    ParagrafVilkår<OmsorgsyterErForelderTilMottakerAvHjelpestønad.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OmsorgsopptjeningGisTilForelderAvBarnMedForhøyetHjelpestønad,
        ).let {
            if (grunnlag.erBarnOgForelder()) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsyter: String,
        val omsorgsytersFamilierelasjoner: Familierelasjoner,
        val omsorgsmottaker: String,
        val omsorgsmottakersFamilierelasjoner: Familierelasjoner,
    ) : ParagrafGrunnlag() {
        fun erBarnOgForelder(): Boolean {
            return omsorgsytersFamilierelasjoner.erBarn(omsorgsmottaker) && omsorgsmottakersFamilierelasjoner.erForelder(omsorgsyter)
        }
    }
}

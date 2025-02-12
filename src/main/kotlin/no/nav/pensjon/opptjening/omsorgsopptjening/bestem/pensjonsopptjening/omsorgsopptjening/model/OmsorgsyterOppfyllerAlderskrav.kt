package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsyterOppfyllerAlderskrav : ParagrafVilkår<AldersvurderingsGrunnlag>() {
    private val ALDERSINTERVALL_OMSORGSYTER: IntRange = 17..69
    override fun vilkarsVurder(grunnlag: AldersvurderingsGrunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<AldersvurderingsGrunnlag>> T.bestemUtfall(grunnlag: AldersvurderingsGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
        ).let {
            if (grunnlag.erOppfylltFor(ALDERSINTERVALL_OMSORGSYTER)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: AldersvurderingsGrunnlag,
        override val utfall: VilkårsvurderingUtfall,
        val gyldigAldersintervall: IntRange = ALDERSINTERVALL_OMSORGSYTER
    ) : ParagrafVurdering<AldersvurderingsGrunnlag>()
}
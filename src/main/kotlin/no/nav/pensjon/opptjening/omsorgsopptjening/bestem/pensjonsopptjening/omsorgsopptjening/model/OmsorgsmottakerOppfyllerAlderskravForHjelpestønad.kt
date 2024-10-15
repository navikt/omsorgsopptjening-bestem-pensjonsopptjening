package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsmottakerOppfyllerAlderskravForHjelpestønad : ParagrafVilkår<AldersvurderingsGrunnlag>() {
    private val ALDERSINTERVALL_HJELPESTØNAD: IntRange = 6..18
    override fun vilkarsVurder(grunnlag: AldersvurderingsGrunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<AldersvurderingsGrunnlag>> T.bestemUtfall(grunnlag: AldersvurderingsGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            JuridiskHenvisning.Folketrygdloven_Kap_6_Paragraf_5_Første_Ledd_Første_Punktum,
            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_11_Tredje_Ledd_Første_Punktum
        ).let {
            if (grunnlag.erOppfylltFor(ALDERSINTERVALL_HJELPESTØNAD)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: AldersvurderingsGrunnlag,
        override val utfall: VilkårsvurderingUtfall,
        val gyldigAldersintervall: IntRange = ALDERSINTERVALL_HJELPESTØNAD
    ) : ParagrafVurdering<AldersvurderingsGrunnlag>()
}

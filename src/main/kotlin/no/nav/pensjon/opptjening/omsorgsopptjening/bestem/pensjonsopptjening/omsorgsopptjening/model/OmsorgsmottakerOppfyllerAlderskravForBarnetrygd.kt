package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

object OmsorgsmottakerOppfyllerAlderskravForBarnetrygd : ParagrafVilkår<AldersvurderingsGrunnlag>() {
    val ALDERSINTERVALL_BARNETRYGD: IntRange = 0..5
    override fun vilkarsVurder(grunnlag: AldersvurderingsGrunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<AldersvurderingsGrunnlag>> T.bestemUtfall(grunnlag: AldersvurderingsGrunnlag): VilkårsvurderingUtfall {
        return setOf(
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Bokstav_a_Første_Punktum
        ).let {
            if (grunnlag.erOppfylltFor(ALDERSINTERVALL_BARNETRYGD)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: AldersvurderingsGrunnlag,
        override val utfall: VilkårsvurderingUtfall,
        val gyldigAldersintervall: IntRange = ALDERSINTERVALL_BARNETRYGD
    ) : ParagrafVurdering<AldersvurderingsGrunnlag>()
}

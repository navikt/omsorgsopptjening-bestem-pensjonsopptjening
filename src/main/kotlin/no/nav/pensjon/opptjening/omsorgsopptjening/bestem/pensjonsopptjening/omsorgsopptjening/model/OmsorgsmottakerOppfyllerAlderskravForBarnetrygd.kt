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
            Referanse.OmsorgsmottakerErIkkeFylt6FørUtgangAvOpptjeningsår
        ).let {
            if (grunnlag.erOppfylltFor(ALDERSINTERVALL_BARNETRYGD)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: AldersvurderingsGrunnlag,
        override val utfall: VilkårsvurderingUtfall,
        val gyldigAldersintervall: IntRange = ALDERSINTERVALL_BARNETRYGD
    ) : ParagrafVurdering<AldersvurderingsGrunnlag>()
}

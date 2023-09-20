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
            Referanse.HjelpestønadYtesTilMedlemUnder18,
            Referanse.OmsorgsopptjeningGisTilOgMedKalenderårHjelpestønadFallerBort
        ).let {
            if (grunnlag.erOppfylltFor(ALDERSINTERVALL_HJELPESTØNAD)) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: AldersvurderingsGrunnlag,
        override val utfall: VilkårsvurderingUtfall,
        val gyldigAldersintervall: IntRange = ALDERSINTERVALL_HJELPESTØNAD
    ) : ParagrafVurdering<AldersvurderingsGrunnlag>()
}

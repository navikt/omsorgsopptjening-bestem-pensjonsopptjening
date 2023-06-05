package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class Og : Vilkar<List<VilkarsVurdering<*>>>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Alle vilkår må være sanne.",
        begrunnelseForInnvilgelse = "Alle vilkår var sanne.",
        begrunnesleForAvslag = "Alle vilkår var ikke sanne."
    ),
    utfallsFunksjon = ogFunksjon
) {

    companion object {
        private val ogFunksjon =
            fun Vilkar<List<VilkarsVurdering<*>>>.(vilkarsVurdering: List<VilkarsVurdering<*>>): VilkårsvurderingUtfall {
                return when {
                    vilkarsVurdering.map { it.utfall }.all { it is VilkårsvurderingUtfall.Innvilget } -> {
                        OgInnvilget(årsak = this.vilkarsInformasjon.begrunnelseForInnvilgelse)
                    }

                    else -> {
                        OgAvslått(årsaker = listOf(AvslagÅrsak.ALLE_VILKÅR_MÅ_VÆRE_OPPFYLT))
                    }
                }
            }

        fun og(vararg vilkarsVurderinger: VilkarsVurdering<*>): OgVurdering {
            return Og().vilkarsVurder(vilkarsVurderinger.toList())
        }
    }

    override fun vilkarsVurder(grunnlag: List<VilkarsVurdering<*>>): OgVurdering {
        return OgVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }

}

data class OgVurdering(
    override val vilkar: Vilkar<List<VilkarsVurdering<*>>>,
    override val grunnlag: List<VilkarsVurdering<*>>,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<List<VilkarsVurdering<*>>>()

data class OgInnvilget(
    val årsak: String,
) : VilkårsvurderingUtfall.Innvilget()

data class OgAvslått(
    override val årsaker: List<AvslagÅrsak>
) : VilkårsvurderingUtfall.Avslag()
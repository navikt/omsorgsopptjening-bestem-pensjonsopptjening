package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class Og : Vilkar<List<VilkarsVurdering<*>>>(
    utfallsFunksjon = ogFunksjon
) {

    companion object {
        private val ogFunksjon =
            fun Vilkar<List<VilkarsVurdering<*>>>.(vilkarsVurdering: List<VilkarsVurdering<*>>): VilkårsvurderingUtfall {
                return when {
                    vilkarsVurdering.map { it.utfall }.all { it is VilkårsvurderingUtfall.Innvilget } -> {
                        OgInnvilget
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
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }

}

data class OgVurdering(
    override val grunnlag: List<VilkarsVurdering<*>>,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<List<VilkarsVurdering<*>>>()

object OgInnvilget : VilkårsvurderingUtfall.Innvilget()

data class OgAvslått(
    override val årsaker: List<AvslagÅrsak>
) : VilkårsvurderingUtfall.Avslag()
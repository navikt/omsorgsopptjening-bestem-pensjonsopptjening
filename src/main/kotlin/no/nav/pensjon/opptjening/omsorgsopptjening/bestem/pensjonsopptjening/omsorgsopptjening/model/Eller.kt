package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class Eller : Vilkar<List<VilkarsVurdering<*>>>(
    utfallsFunksjon = ellerFunksjon
) {

    companion object {
        private val ellerFunksjon =
            fun Vilkar<List<VilkarsVurdering<*>>>.(vilkarsVurdering: List<VilkarsVurdering<*>>): VilkårsvurderingUtfall {
                return when {
                    vilkarsVurdering.map { it.utfall }.any { it is VilkårsvurderingUtfall.Innvilget } -> {
                        EllerInnvilget
                    }

                    else -> {
                        EllerAvslått(listOf(AvslagÅrsak.MINST_ET_VILKÅR_MÅ_VÆRE_OPPFYLT))
                    }
                }
            }

        fun eller(vararg vilkarsVurderinger: VilkarsVurdering<*>): EllerVurdering {
            return Eller().vilkarsVurder(vilkarsVurderinger.toList())
        }

        fun eller(vilkarsVurderinger: List<VilkarsVurdering<*>>): EllerVurdering {
            return Eller().vilkarsVurder(vilkarsVurderinger.toList())
        }

        fun <Input, Vurdering : VilkarsVurdering<*>> Iterable<Input>.minstEn(mappingFunction: (Input) -> Vurdering): EllerVurdering {
            return eller(map { mappingFunction.invoke(it) })
        }
    }

    override fun vilkarsVurder(grunnlag: List<VilkarsVurdering<*>>): EllerVurdering {
        return EllerVurdering(
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag)
        )
    }
}

data class EllerVurdering(
    override val grunnlag: List<VilkarsVurdering<*>>,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<List<VilkarsVurdering<*>>>()

object EllerInnvilget : VilkårsvurderingUtfall.Innvilget()

data class EllerAvslått(
    override val årsaker: List<AvslagÅrsak>,
) : VilkårsvurderingUtfall.Avslag()
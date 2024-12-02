package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class Eller : Vilkar<List<VilkarsVurdering<*>>>() {

    companion object {
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
            utfall = bestemUtfall(grunnlag)
        )
    }

    override fun <T : Vilkar<List<VilkarsVurdering<*>>>> T.bestemUtfall(grunnlag: List<VilkarsVurdering<*>>): VilkårsvurderingUtfall {
        val alleUtfall = grunnlag.map { it.utfall }
        return when {
            alleUtfall.any { it.erInnvilget() } -> {
                EllerInnvilget
            }

            alleUtfall.none { it.erInnvilget() } && alleUtfall.any { it.erUbestemt() } -> {
                EllerUbestemt
            }

            else -> {
                EllerAvslått
            }
        }
    }

}

data class EllerVurdering(
    override val grunnlag: List<VilkarsVurdering<*>>,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<List<VilkarsVurdering<*>>>()

data object EllerInnvilget : VilkårsvurderingUtfall.Innvilget()
data object EllerUbestemt : VilkårsvurderingUtfall.Ubestemt()
data object EllerAvslått : VilkårsvurderingUtfall.Avslag()
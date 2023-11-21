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
        return when {
            grunnlag.map { it.utfall }.any { it.erInnvilget() } -> {
                EllerInnvilget
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

data object EllerAvslått : VilkårsvurderingUtfall.Avslag()
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class Og : Vilkar<List<VilkarsVurdering<*>>>() {

    companion object {
        fun og(vararg vilkarsVurderinger: VilkarsVurdering<*>): OgVurdering {
            return Og().vilkarsVurder(vilkarsVurderinger.toList())
        }
    }

    override fun vilkarsVurder(grunnlag: List<VilkarsVurdering<*>>): OgVurdering {
        return OgVurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<List<VilkarsVurdering<*>>>> T.bestemUtfall(grunnlag: List<VilkarsVurdering<*>>): VilkårsvurderingUtfall {
        val alleUtfall = grunnlag.map { it.utfall }
        return when {
            alleUtfall.all { it.erInnvilget() } -> {
                OgInnvilget
            }

            alleUtfall.none { it.erAvslag() } && alleUtfall.any { it.erUbestemt() } -> {
                OgUbestemt
            }

            else -> {
                OgAvslått
            }
        }
    }

}

data class OgVurdering(
    override val grunnlag: List<VilkarsVurdering<*>>,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<List<VilkarsVurdering<*>>>()

data object OgInnvilget : VilkårsvurderingUtfall.Innvilget()
data object OgUbestemt : VilkårsvurderingUtfall.Ubestemt()
data object OgAvslått : VilkårsvurderingUtfall.Avslag()
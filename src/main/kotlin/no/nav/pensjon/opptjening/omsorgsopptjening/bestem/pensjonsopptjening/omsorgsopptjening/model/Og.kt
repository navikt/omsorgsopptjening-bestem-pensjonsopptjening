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
        return when {
            grunnlag.map { it.utfall }.all { it.erInnvilget() } -> {
                OgInnvilget
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

object OgInnvilget : VilkårsvurderingUtfall.Innvilget()

object OgAvslått: VilkårsvurderingUtfall.Avslag()
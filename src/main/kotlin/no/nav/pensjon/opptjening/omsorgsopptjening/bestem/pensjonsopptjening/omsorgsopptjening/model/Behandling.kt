package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

data class Behandling(
    private val grunnlag: BarnetrygdGrunnlag,
) {
    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsmottaker() = grunnlag.omsorgsmottaker
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = grunnlag.omsorgstype
    fun grunnlag() = grunnlag

    fun utfall(): AutomatiskGodskrivingUtfall {
        return when (vilkårsvurdering().utfall is VilkårsvurderingUtfall.Innvilget) {
            true -> {
                AutomatiskGodskrivingUtfall.Innvilget(
                    omsorgsmottaker = omsorgsmottaker()
                )
            }

            false -> {
                AutomatiskGodskrivingUtfall.Avslag(
                    omsorgsmottaker = omsorgsmottaker(),
                    årsaker = finnÅrsakerForAvslag()
                )
            }
        }
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            OmsorgsYterOver16Ar().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr()),
            OmsorgsyterUnder70Ar().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr()),
            FullOmsorgForBarnUnder6().vilkarsVurder(grunnlag.fullOmsorg())
        )
    }

    private fun finnAlleVilkårsvurderinger(): List<VilkarsVurdering<*>> {
        return UnwrapOgEllerVisitor.unwrap(vilkårsvurdering())
    }

    private fun finnÅrsakerForAvslag(): List<AvslagÅrsak> {
        return finnAlleVilkårsvurderinger()
            .map { it.utfall }
            .filterIsInstance<VilkårsvurderingUtfall.Avslag>()
            .flatMap { it.årsaker }

    }
}




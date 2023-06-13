package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

data class Behandling(
    private val grunnlag: BarnetrygdGrunnlag,
    private val vilkarFactory: VilkarFactory
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
                    omsorgsmottaker = grunnlag.omsorgsmottaker
                )
            }

            false -> {
                AutomatiskGodskrivingUtfall.Avslag(
                    omsorgsmottaker = grunnlag.omsorgsmottaker,
                    årsaker = finnÅrsakerForAvslag()
                )
            }
        }
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            vilkarFactory.omsorgsyterOver16Ar(),
            vilkarFactory.omsorgsyterUnder70Ar(),
            vilkarFactory.fullOmsorgForBarnUnder6(),
            vilkarFactory.kanKunGodskrivesEnOmsorgsyter()
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




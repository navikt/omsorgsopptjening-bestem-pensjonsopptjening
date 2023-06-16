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
        vilkårsvurdering().let {
            return when (it.utfall.erInnvilget()) {
                true -> {
                    AutomatiskGodskrivingUtfall.Innvilget(oppsummering())
                }

                false -> {
                    AutomatiskGodskrivingUtfall.Avslag(oppsummering())
                }
            }
        }
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            vilkarFactory.omsorgsyterOver16Ar(),
            vilkarFactory.omsorgsyterUnder70Ar(),
            vilkarFactory.omsorgsmottakerIkkeFylt6Ar(),
            vilkarFactory.fullOmsorgForBarnUnder6(),
            vilkarFactory.kanKunGodskrivesEnOmsorgsyter(),
            vilkarFactory.kanKunGodskrivesEtBarnPerÅr()
        )
    }

    private fun oppsummering(): Behandlingsoppsummering {
        return finnAlleVilkårsvurderinger()
            .flatMap { vv ->
                vv.utfall.paragrafer().map {
                    it to vv.utfall.erInnvilget()
                }
            }.map {
                ParagrafOppsummering(it)
            }.let {
                Behandlingsoppsummering(it)
            }
    }

    private fun finnAlleVilkårsvurderinger(): List<VilkarsVurdering<*>> {
        return UnwrapOgEllerVisitor.unwrap(vilkårsvurdering())
    }

    private fun finnAvslagsparagrafer(): Set<Paragraf> {
        return finnAlleVilkårsvurderinger()
            .map { it.utfall }
            .filterIsInstance<VilkårsvurderingUtfall.Avslag>()
            .flatMap { it.paragrafer() }
            .toSet()
    }
}




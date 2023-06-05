package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.BeriketOmsorgsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Eller.Companion.minstEn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

data class Behandling(
    private val grunnlag: BeriketOmsorgsgrunnlag,
) {
    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = grunnlag.omsorgstype
    fun grunnlag() = grunnlag

    fun utfall(): AutomatiskGodskrivingUtfall {
        return when (vilkårsvurdering().utfall is VilkårsvurderingUtfall.Innvilget) {
            true -> {
                AutomatiskGodskrivingUtfall.Innvilget(omsorgsmottaker = finnInnvilgetFullOmsorg().omsorgsmottaker)
            }

            false -> {
                AutomatiskGodskrivingUtfall.Avslag(årsaker = finnÅrsakerForAvslag())
            }
        }
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            OmsorgsYterOver16Ar().vilkarsVurder(
                OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsAr = omsorgsår(),
                    omsorgsyter = omsorgsyter()
                )
            ),
            OmsorgsyterUnder70Ar().vilkarsVurder(
                OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsAr = omsorgsår(),
                    omsorgsyter = omsorgsyter()
                )
            ),
            grunnlag.omsorgsmottakere().minstEn { omsorgsmottaker ->
                FullOmsorgForBarnUnder6().vilkarsVurder(
                    FullOmsorgForBarnUnder6Grunnlag(
                        omsorgsAr = omsorgsår(),
                        omsorgsmottaker = omsorgsmottaker,
                        antallMånederFullOmsorg = grunnlag.antallMånederFullOmsorgForMottaker(omsorgsmottaker)
                    )
                )
            }
        )
    }

    private fun finnAlleVilkårsvurderinger(): List<VilkarsVurdering<*>> {
        return UnwrapOgEllerVisitor.unwrap(vilkårsvurdering())
    }

    private fun finnInnvilgetFullOmsorg(): FullOmsorgForBarnUnder6Innvilget {
        return finnAlleVilkårsvurderinger()
            .filterIsInstance<FullOmsorgForBarnUnder6Vurdering>()
            .map { it.utfall }
            .filterIsInstance<FullOmsorgForBarnUnder6Innvilget>()
            .minBy { it.omsorgsmottaker.fodselsAr } //TODO finn en eller annen sortering her
    }

    private fun finnÅrsakerForAvslag(): List<AvslagÅrsak> {
        return finnAlleVilkårsvurderinger()
            .map { it.utfall }
            .filterIsInstance<VilkårsvurderingUtfall.Avslag>()
            .flatMap { it.årsaker }

    }
}




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
        return vilkårsvurdering().let {
            when (it.utfall.erInnvilget()) {
                true -> {
                    AutomatiskGodskrivingUtfall.Innvilget
                }

                false -> {
                    if (it.erEnesteAvslag<FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.Vurdering>()) {
                        AutomatiskGodskrivingUtfall.AvslagMedOppgave
                    } else {
                        AutomatiskGodskrivingUtfall.AvslagUtenOppgave
                    }
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
            vilkarFactory.fullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder(),
            vilkarFactory.kanKunGodskrivesEnOmsorgsyter(),
            vilkarFactory.kanKunGodskrivesEtBarnPerÅr()
        )
    }
}



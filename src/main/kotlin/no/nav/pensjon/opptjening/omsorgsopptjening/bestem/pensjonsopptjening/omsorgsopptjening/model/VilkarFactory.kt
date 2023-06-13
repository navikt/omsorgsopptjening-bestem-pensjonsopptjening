package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo


class VilkarFactoryImpl(val grunnlag: BarnetrygdGrunnlag,
                        val behandlingRepo: BehandlingRepo): VilkarFactory {
    override fun omsorgsyterOver16Ar(): OmsorgsyterOver16ArVurdering {
        return OmsorgsYterOver16Ar().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun omsorgsyterUnder70Ar(): OmsorgsyterUnder70Vurdering {
        return OmsorgsyterUnder70Ar().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6Vurdering {
        return FullOmsorgForBarnUnder6().vilkarsVurder(grunnlag.fullOmsorg())

    }

    override fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyterVurdering {
         return KanKunGodskrivesEnOmsorgsyter().vilkarsVurder(behandlingRepo.finnForOmsorgsmottakerOgAr(grunnlag.omsorgsmottaker.fnr, grunnlag.omsorgsAr)
             .map { BehandlingsIdUtfall(it.id, it.utfall.erInnvilget()) }
             .let { KanKunGodskrivesEnOmsorgsyterGrunnlag(it) })
    }
    private fun BehandlingUtfall.erInnvilget(): Boolean {
        return when(this){
            is AutomatiskGodskrivingUtfall.Avslag -> false
            is AutomatiskGodskrivingUtfall.Innvilget -> true
        }
    }

}

interface VilkarFactory {

    fun omsorgsyterOver16Ar(): OmsorgsyterOver16ArVurdering
    fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyterVurdering
    fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6Vurdering
    fun omsorgsyterUnder70Ar(): OmsorgsyterUnder70Vurdering

}
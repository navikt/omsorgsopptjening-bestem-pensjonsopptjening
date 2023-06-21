package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo


internal class VilkarFactoryImpl(
    private val grunnlag: BarnetrygdGrunnlag,
    private val behandlingRepo: BehandlingRepo
) : VilkarFactory {
    override fun omsorgsyterOver16Ar(): OmsorgsyterFylt17ÅrVurdering {
        return OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun omsorgsyterUnder70Ar(): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering {
        return OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }
    override fun omsorgsmottakerIkkeFylt6Ar(): OmsorgsmottakerIkkeFylt6ArVurdering {
        return OmsorgsmottakerIkkeFylt6Ar().vilkarsVurder(grunnlag.forOmsorgsmottakerOgÅr())
    }



    override fun kanKunGodskrivesEtBarnPerÅr(): KanKunGodskrivesEtBarnPerÅrVurdering {
        return KanKunGodskrivesEtBarnPerÅr().vilkarsVurder(
            behandlingRepo.finnForOmsorgsyterOgAr(
                fnr = grunnlag.omsorgsyter.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                AndreBehandlinger(
                    behandlingsId = it.id,
                    år = it.omsorgsAr,
                    omsorgsmottaker = it.omsorgsmottaker,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let {
                KanKunGodskrivesEtBarnPerÅrGrunnlag(
                    omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                    behandlinger = it
                )
            })
    }

    override fun liktAntallMånederOmsorg(): LiktAntallMånederOmsorg {
        return LiktAntallMånederOmsorg().vilkarsVurder(grunnlag.)
    }

    override fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6Vurdering {
        return FullOmsorgForBarnUnder6().vilkarsVurder(grunnlag.fullOmsorg())

    }

    override fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyterVurdering {
        return KanKunGodskrivesEnOmsorgsyter().vilkarsVurder(
            behandlingRepo.finnForOmsorgsmottakerOgAr(
                omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                BehandlingsIdUtfall(
                    behandlingsId = it.id,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let { KanKunGodskrivesEnOmsorgsyterGrunnlag(it) })
    }
}

interface VilkarFactory {

    fun omsorgsyterOver16Ar(): OmsorgsyterFylt17ÅrVurdering
    fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyterVurdering
    fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6Vurdering
    fun omsorgsyterUnder70Ar(): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering
    fun omsorgsmottakerIkkeFylt6Ar(): OmsorgsmottakerIkkeFylt6ArVurdering

    fun kanKunGodskrivesEtBarnPerÅr(): KanKunGodskrivesEtBarnPerÅrVurdering

    fun liktAntallMånederOmsorg(): LiktAntallMånederOmsorg

}
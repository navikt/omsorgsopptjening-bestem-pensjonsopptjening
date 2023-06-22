package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo


internal class VilkarFactoryImpl(
    private val grunnlag: BarnetrygdGrunnlag,
    private val behandlingRepo: BehandlingRepo
) : VilkarFactory {
    override fun omsorgsyterOver16Ar(): OmsorgsyterFylt17VedUtløpAvOmsorgsår.Vurdering {
        return OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun omsorgsyterUnder70Ar(): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering {
        return OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun omsorgsmottakerIkkeFylt6Ar(): OmsorgsmottakerIkkeFylt6Ar.Vurdering {
        return OmsorgsmottakerIkkeFylt6Ar.vilkarsVurder(grunnlag.forOmsorgsmottakerOgÅr())
    }


    override fun kanKunGodskrivesEtBarnPerÅr(): KanKunGodskrivesEtBarnPerÅr.Vurdering {
        return KanKunGodskrivesEtBarnPerÅr.vilkarsVurder(
            behandlingRepo.finnForOmsorgsyterOgAr(
                fnr = grunnlag.omsorgsyter.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                KanKunGodskrivesEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                    behandlingsId = it.id,
                    år = it.omsorgsAr,
                    omsorgsmottaker = it.omsorgsmottaker,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let {
                KanKunGodskrivesEtBarnPerÅr.Grunnlag(
                    omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                    behandlinger = it
                )
            })
    }

    override fun fullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder(): FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.Vurdering {
        return FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.vilkarsVurder(
            FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.Grunnlag(
                fullOmsorgForBarnUnder6Vurdering = fullOmsorgForBarnUnder6(),
                liktAntallMånederOmsorgVurdering = liktAntallMånederOmsorg()
            )
        )
    }

    fun liktAntallMånederOmsorg(): LiktAntallMånederOmsorg.Vurdering {
        return LiktAntallMånederOmsorg.vilkarsVurder(grunnlag.liktAntallMånederOmsorgGrunnlag())
    }

    override fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6.Vurdering {
        return FullOmsorgForBarnUnder6.vilkarsVurder(grunnlag.fullOmsorg())

    }

    override fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyter.Vurdering {
        return KanKunGodskrivesEnOmsorgsyter.vilkarsVurder(
            behandlingRepo.finnForOmsorgsmottakerOgAr(
                omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                KanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
                    behandlingsId = it.id,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let { KanKunGodskrivesEnOmsorgsyter.Grunnlag(it) })
    }
}

interface VilkarFactory {

    fun omsorgsyterOver16Ar(): OmsorgsyterFylt17VedUtløpAvOmsorgsår.Vurdering
    fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyter.Vurdering
    fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6.Vurdering
    fun omsorgsyterUnder70Ar(): OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering
    fun omsorgsmottakerIkkeFylt6Ar(): OmsorgsmottakerIkkeFylt6Ar.Vurdering

    fun kanKunGodskrivesEtBarnPerÅr(): KanKunGodskrivesEtBarnPerÅr.Vurdering

    fun fullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder(): FullOmsorgForBarnUnder6OgIngenHarLiktAntallMåneder.Vurdering
}
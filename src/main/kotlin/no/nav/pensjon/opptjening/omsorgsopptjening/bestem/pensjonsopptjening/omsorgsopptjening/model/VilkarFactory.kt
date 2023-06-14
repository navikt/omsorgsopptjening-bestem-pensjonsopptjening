package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo


internal class VilkarFactoryImpl(
    private val grunnlag: BarnetrygdGrunnlag,
    private val behandlingRepo: BehandlingRepo
) : VilkarFactory {
    override fun omsorgsyterOver16Ar(): OmsorgsyterOver16ArVurdering {
        return OmsorgsYterOver16Ar().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun omsorgsyterUnder70Ar(): OmsorgsyterUnder70Vurdering {
        return OmsorgsyterUnder70Ar().vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

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

    fun omsorgsyterOver16Ar(): OmsorgsyterOver16ArVurdering
    fun kanKunGodskrivesEnOmsorgsyter(): KanKunGodskrivesEnOmsorgsyterVurdering
    fun fullOmsorgForBarnUnder6(): FullOmsorgForBarnUnder6Vurdering
    fun omsorgsyterUnder70Ar(): OmsorgsyterUnder70Vurdering

    fun kanKunGodskrivesEtBarnPerÅr(): KanKunGodskrivesEtBarnPerÅrVurdering

}
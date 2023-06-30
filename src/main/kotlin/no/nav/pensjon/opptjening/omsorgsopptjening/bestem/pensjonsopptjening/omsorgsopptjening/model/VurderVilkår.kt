package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo

interface VurderVilkår {

    fun OmsorgsyterErFylt17VedUtløpAvOmsorgsår(): OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering
    fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering
    fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering
    fun OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(): OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering
    fun OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(): OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering

    fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering
    fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(): VilkarsVurdering<*>
}

internal class VilkårsvurderingFactory(
    private val grunnlag: BarnetrygdGrunnlag,
    private val behandlingRepo: BehandlingRepo
) : VurderVilkår {
    override fun OmsorgsyterErFylt17VedUtløpAvOmsorgsår(): OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering {
        return OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(): OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering {
        return OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(grunnlag.forOmsorgsyterOgÅr())

    }

    override fun OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(): OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering {
        return OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.vilkarsVurder(grunnlag.forOmsorgsmottakerOgÅr())
    }


    override fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering {
        return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            behandlingRepo.finnForOmsorgsyterOgAr(
                fnr = grunnlag.omsorgsyter.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.FullførtBehandlingForOmsorgsyter(
                    behandlingsId = it.id,
                    omsorgsyter = it.omsorgsyter,
                    omsorgsÅr = it.omsorgsAr,
                    omsorgsmottaker = it.omsorgsmottaker,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let {
                OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                    omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                    omsorgsår = grunnlag.omsorgsAr,
                    behandlinger = it
                )
            })
    }

    override fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(): VilkarsVurdering<*> {
        return OmsorgstyerHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            grunnlag.summertAntallMånederPerOmsorgsyter()
        )
    }
    override fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering {
        return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(grunnlag.tilstrekkeligOmsorgsarbeid())

    }

    override fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering {
        return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.vilkarsVurder(
            behandlingRepo.finnForOmsorgsmottakerOgAr(
                omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag.FullførtBehandlingForOmsorgsmottaker(
                    behandlingsId = it.id,
                    omsorgsyter = it.omsorgsyter,
                    omsorgsmottaker = it.omsorgsmottaker,
                    omsorgsår = it.omsorgsAr,
                    erInnvilget = it.erInnvilget(),
                )
            }.let {
                OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag(
                    omsorgsår = grunnlag.omsorgsAr,
                    fullførteBehandlinger = it
                )
            })
    }
}
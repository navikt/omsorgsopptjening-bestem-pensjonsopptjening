package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo

interface VurderVilkår {

    fun OmsorgsyterErFylt17VedUtløpAvOmsorgsår(): OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering
    fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Vurdering
    fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering
    fun OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(): OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering
    fun OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(): OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering

    fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering

    fun OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Vurdering
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
                OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag.AndreBehandlinger(
                    behandlingsId = it.id,
                    år = it.omsorgsAr,
                    omsorgsmottaker = it.omsorgsmottaker,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let {
                OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag(
                    omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                    behandlinger = it
                )
            })
    }

    override fun OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Vurdering {
        return OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.vilkarsVurder(
            OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Grunnlag(
                fullOmsorgForBarnUnder6Vurdering = OmsorgsyterHarTilstrekkeligOmsorgsarbeid(),
                liktAntallMånederOmsorgVurdering = OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere()
            )
        )
    }

    private fun OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere(): OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.Vurdering {
        return OmsorgsopptjeningKanIkkeGisHvisTilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere.vilkarsVurder(
            grunnlag.tilnærmetLikeMyeOmsorgsarbeidBlantFlereOmsorgsytere()
        )
    }

    override fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering {
        return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(grunnlag.tilstrekkeligOmsorgsarbeid())

    }

    override fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Vurdering {
        return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.vilkarsVurder(
            behandlingRepo.finnForOmsorgsmottakerOgAr(
                omsorgsmottaker = grunnlag.omsorgsmottaker.fnr,
                ar = grunnlag.omsorgsAr
            ).map {
                OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag.BehandlingsIdUtfall(
                    behandlingsId = it.id,
                    erInnvilget = it.utfall.erInnvilget()
                )
            }.let { OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Grunnlag(it) })
    }
}
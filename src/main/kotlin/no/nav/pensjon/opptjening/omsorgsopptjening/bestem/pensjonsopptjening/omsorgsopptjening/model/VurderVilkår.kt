package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo

interface VurderVilkår {

    fun OmsorgsyterErFylt17VedUtløpAvOmsorgsår(): OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering
    fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering
    fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering
    fun OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(): OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering
    fun OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(): OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering
    fun OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(): OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering
    fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering
    fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering
    fun OmsorgsyterErForelderTilMottakerAvHjelpestønad(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering
}

internal class VilkårsvurderingFactory(
    private val grunnlag: OmsorgsopptjeningGrunnlag,
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
    override fun OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(): OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering {
        return OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.vilkarsVurder(grunnlag.forOmsorgsmottakerOgÅr())
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

    override fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering {
        return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(
            grunnlag.forSummertOmsorgPerOmsorgsyter()
        )
    }

    override fun OmsorgsyterErForelderTilMottakerAvHjelpestønad(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering {
        return OmsorgsyterErForelderTilMottakerAvHjelpestønad.vilkarsVurder(grunnlag.forFamilierelasjon())
    }

    override fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering {
        return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(grunnlag.forTilstrekkeligOmsorgsarbeid())

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
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

interface VurderVilkår {
    fun OmsorgsyterOppfyllerAlderskrav(): OmsorgsyterOppfyllerAlderskrav.Vurdering
    fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering
    fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering
    fun OmsorgsmottakerOppfyllerAlderskravForBarnetryg(): OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering
    fun OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(): OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering
    fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering
    fun OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering
    fun OmsorgsyterErForelderTilMottakerAvHjelpestønad(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering
    fun OmsorgsyterMottarBarnetrgyd(): OmsorgsyterMottarBarnetrgyd.Vurdering
    fun OmsorgsyterHarGyldigOmsorgsarbeid(): OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering
    fun OmsorgsyterErMedlemIFolketrygden(): OmsorgsyterErMedlemIFolketrygden.Vurdering
    fun OmsorgsyterErIkkeOmsorgsmottaker(): OmsorgsyterErikkeOmsorgsmottaker.Vurdering
    fun OmsorgsyterHarIkkeDødsdato(): OmsorgsyterHarIkkeDødsdato.Vurdering
}

internal class VilkårsvurderingFactory(
    private val grunnlag: OmsorgsopptjeningGrunnlag,
    private val finnForOmsorgsyterOgÅr: () -> List<FullførtBehandling>,
    private val finnForOmsorgsmottakerOgÅr: () -> List<FullførtBehandling>
) : VurderVilkår {
    override fun OmsorgsyterOppfyllerAlderskrav(): OmsorgsyterOppfyllerAlderskrav.Vurdering {
        return OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(grunnlag.forAldersvurderingOmsorgsyter())
    }

    override fun OmsorgsmottakerOppfyllerAlderskravForBarnetryg(): OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.Vurdering {
        return OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.vilkarsVurder(grunnlag.forAldersvurderingOmsorgsmottaker())
    }

    override fun OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(): OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.Vurdering {
        return OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.vilkarsVurder(grunnlag.forAldersvurderingOmsorgsmottaker())
    }


    override fun OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(): OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering {
        return OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.vilkarsVurder(
            finnForOmsorgsyterOgÅr().map {
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
        return OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.vilkarsVurder(grunnlag.forGyldigOmsorgsarbeidPerOmsorgsyter())
    }

    override fun OmsorgsyterErForelderTilMottakerAvHjelpestønad(): OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering {
        return OmsorgsyterErForelderTilMottakerAvHjelpestønad.vilkarsVurder(grunnlag.forFamilierelasjon())
    }

    override fun OmsorgsyterMottarBarnetrgyd(): OmsorgsyterMottarBarnetrgyd.Vurdering {
        return OmsorgsyterMottarBarnetrgyd.vilkarsVurder(grunnlag.forMottarBarnetrygd())
    }

    override fun OmsorgsyterHarGyldigOmsorgsarbeid(): OmsorgsyterHarGyldigOmsorgsarbeid.Vurdering {
        return OmsorgsyterHarGyldigOmsorgsarbeid.vilkarsVurder(grunnlag.forGyldigOmsorgsarbeid(grunnlag.omsorgsyter))
    }

    override fun OmsorgsyterErMedlemIFolketrygden(): OmsorgsyterErMedlemIFolketrygden.Vurdering {
        return OmsorgsyterErMedlemIFolketrygden.vilkarsVurder(grunnlag.forMedlemskapIFolketrygden())
    }

    override fun OmsorgsyterErIkkeOmsorgsmottaker(): OmsorgsyterErikkeOmsorgsmottaker.Vurdering {
        return OmsorgsyterErikkeOmsorgsmottaker.vilkarsVurder(grunnlag.forOmsorgsyterErIkkeOmsorgsmottaker())
    }

    override fun OmsorgsyterHarIkkeDødsdato(): OmsorgsyterHarIkkeDødsdato.Vurdering {
        return OmsorgsyterHarIkkeDødsdato.vilkarsVurder(grunnlag.forOmsorgsyterHarIkkeDødsdato())
    }

    override fun OmsorgsyterHarTilstrekkeligOmsorgsarbeid(): OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering {
        return OmsorgsyterHarTilstrekkeligOmsorgsarbeid.vilkarsVurder(grunnlag.forTilstrekkeligOmsorgsarbeid())

    }

    override fun OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(): OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering {
        return OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.vilkarsVurder(
            finnForOmsorgsmottakerOgÅr().map {
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
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import java.util.UUID

/**
 * En automatisk vurdering av en [omsorgsyter]s rett til omsorgsopptjening på bakgrunn av omsorgsarbeid
 * ytt for [omsorgsmottaker] i et gitt [omsorgsår].
 */
data class Behandling(
    private val grunnlag: OmsorgsopptjeningGrunnlag,
    private val vurderVilkår: VurderVilkår,
    private val meldingId: UUID
) {
    val vilkarsVurdering = vilkårsvurdering()
    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsmottaker() = grunnlag.omsorgsmottaker
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = grunnlag.omsorgstype
    fun grunnlag() = grunnlag

    fun meldingId() = meldingId

    fun utfall(): BehandlingUtfall {
        return AvgjørBehandlingUtfall(vilkarsVurdering).utfall()
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            vilkårsvurderOmsorgsyter(),
            when (omsorgstype()) {
                DomainOmsorgskategori.BARNETRYGD -> {
                    vilkårsvurderBarnetrygd()
                }

                DomainOmsorgskategori.HJELPESTØNAD -> {
                    vilkårsurderHjelpestønad()
                }
            }
        )
    }

    private fun vilkårsvurderBarnetrygd(): VilkarsVurdering<*> {
        return vurderVilkår.OmsorgsmottakerOppfyllerAlderskravForBarnetryg()
    }

    private fun vilkårsurderHjelpestønad(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsmottakerOppfyllerAlderskravForHjelpestønad(),
            /**
             * Ikke et kriterium i bpen030
             */
            //vurderVilkår.OmsorgsyterErForelderTilMottakerAvHjelpestønad()
        )
    }

    private fun vilkårsvurderOmsorgsyter(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsyterOppfyllerAlderskrav(),
            vurderVilkår.OmsorgsyterMottarBarnetrgyd(),
            vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarGyldigOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr(),
            vurderVilkår.OmsorgsyterErMedlemIFolketrygden(),
        )
    }

}



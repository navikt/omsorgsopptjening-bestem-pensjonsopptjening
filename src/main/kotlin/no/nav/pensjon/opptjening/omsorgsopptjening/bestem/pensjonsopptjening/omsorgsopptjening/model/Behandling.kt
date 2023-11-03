package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
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
        return vilkarsVurdering.let { vilkårsvurdering ->
            when (vilkårsvurdering.utfall.erInnvilget()) {
                true -> BehandlingUtfall.Innvilget
                false -> {
                    if (behovForManuellBehandling()) {
                        BehandlingUtfall.Manuell
                    } else {
                        BehandlingUtfall.Avslag
                    }
                }
            }
        }
    }

    fun behovForManuellBehandling(): Boolean {
        //TODO utvide med andre feilsituasjoner som må sjekkes manuelt?
        return vilkarsVurdering.behovForManuellBehandling()
    }


    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            vilkårsvurderOmsorgsyter(),
            when (omsorgstype()) {
                DomainOmsorgstype.BARNETRYGD -> {
                    vilkårsvurderBarnetrygd()
                }

                DomainOmsorgstype.HJELPESTØNAD -> {
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
             * @see FullførtBehandling.sendBrev
             * Ikke et kriterium i bpen030
             */
            //vurderVilkår.OmsorgsyterErForelderTilMottakerAvHjelpestønad()
        )
    }

    private fun vilkårsvurderOmsorgsyter(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsyterOppfyllerAlderskrav(),
            vurderVilkår.OmsorgsyterErMedlemAvFolketrygden(),
            vurderVilkår.OmsorgsyterMottarBarnetrgyd(),
            vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarGyldigOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr()
        )
    }

}



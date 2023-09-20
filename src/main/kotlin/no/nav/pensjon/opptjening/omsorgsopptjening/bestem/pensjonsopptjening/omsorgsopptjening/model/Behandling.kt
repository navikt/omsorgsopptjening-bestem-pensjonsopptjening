package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
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
    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsmottaker() = grunnlag.omsorgsmottaker
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeid().omsorgstype()
    fun grunnlag() = grunnlag

    fun meldingId() = meldingId

    fun utfall(): BehandlingUtfall {
        return vilkårsvurdering().let { vilkårsvurdering ->
            when (vilkårsvurdering.utfall.erInnvilget()) {
                true -> BehandlingUtfall.Innvilget
                false -> BehandlingUtfall.Avslag
            }
        }
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
            vurderVilkår.OmsorgsyterErForelderTilMottakerAvHjelpestønad(),
        )
    }

    private fun vilkårsvurderOmsorgsyter(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsyterOppfyllerAlderskrav(),
            vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr()
        )
    }

}



package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

data class Behandling(
    private val grunnlag: BarnetrygdGrunnlag,
    private val vurderVilkår: VurderVilkår
) {
    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsmottaker() = grunnlag.omsorgsmottaker
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = grunnlag.omsorgstype
    fun grunnlag() = grunnlag

    fun utfall(): AutomatiskGodskrivingUtfall {
        return vilkårsvurdering().let {
            when (it.utfall.erInnvilget()) {
                true -> {
                    AutomatiskGodskrivingUtfall.Innvilget
                }

                false -> {
                    if (it.erEnesteAvslag<OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Vurdering>()) {
                        AutomatiskGodskrivingUtfall.AvslagMedOppgave
                    } else {
                        AutomatiskGodskrivingUtfall.AvslagUtenOppgave
                    }
                }
            }
        }
    }

    fun vilkårsvurdering(): VilkarsVurdering<*> {
        return og(
            vurderVilkår.OmsorgsyterErFylt17VedUtløpAvOmsorgsår(),
            vurderVilkår.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår(),
            vurderVilkår.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår(),
            vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeid(),
            vurderVilkår.OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr()
        )
    }
}



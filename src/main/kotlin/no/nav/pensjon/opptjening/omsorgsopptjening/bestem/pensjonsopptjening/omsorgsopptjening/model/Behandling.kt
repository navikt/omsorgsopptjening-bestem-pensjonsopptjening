package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import java.util.UUID

data class Behandling(
    private val grunnlag: BarnetrygdGrunnlag,
    private val vurderVilkår: VurderVilkår,
    private val kafkaMeldingId: UUID
) {
    fun omsorgsår() = grunnlag.omsorgsAr
    fun omsorgsmottaker() = grunnlag.omsorgsmottaker
    fun omsorgsyter() = grunnlag.omsorgsyter
    fun omsorgstype() = grunnlag.omsorgstype
    fun grunnlag() = grunnlag

    fun kafkaMeldingId() = kafkaMeldingId

    fun utfall(): AutomatiskGodskrivingUtfall {
        return vilkårsvurdering().let { vilkårsvurdering ->
            when (vilkårsvurdering.utfall.erInnvilget()) {
                true -> {
                    AutomatiskGodskrivingUtfall.Innvilget
                }

                false -> {
                    if (vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()) {
                        vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                            if (it.grunnlag.flereHarLikeMange()) {
                                AutomatiskGodskrivingUtfall.AvslagMedOppgave
                            } else {
                                AutomatiskGodskrivingUtfall.AvslagUtenOppgave
                            }
                        }
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
            vurderVilkår.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr(),
            vurderVilkår.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr()
        )
    }
}



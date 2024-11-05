package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import java.util.UUID

object OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn :
    ParagrafVilkår<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_3_Første_Ledd_Første_Punktum,
            JuridiskHenvisning.Forskrift_Om_Alderspensjon_I_Folketrygden_Kap_3_Paragraf_4_Første_Ledd_Første_Punktum,
        ).let { referanser ->
            when {
                grunnlag.erManuell() -> {
                    VilkårsvurderingUtfall.Ubestemt(referanser)
                }

                else -> {
                    VilkårsvurderingUtfall.Innvilget.Vilkår(referanser)
                }
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>() {
        override fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger {
            return Oppgaveopplysninger.Generell(
                oppgavemottaker = behandling.omsorgsyter,
                oppgaveTekst = Oppgave.annenForelderInnvilgetOmsorgsopptjeningForAnnetFellesbarn(
                    omsorgsmottaker = behandling.omsorgsmottaker,
                    annenForelderOgBarn = grunnlag.finnManuelle().map { it.omsorgsyter to it.omsorgsmottaker }.toSet()
                )
            )
        }
    }

    data class Grunnlag(
        val omsorgsmottaker: String,
        val omsorgsår: Int,
        val behandlinger: List<FullførtBehandlingForAnnenOmsorgsmottaker>
    ) : ParagrafGrunnlag() {
        init {
            require(behandlinger.none { it.omsorgsmottaker == omsorgsmottaker }) { "Skal ikke inneholde behandlinger for omsorgsmottaker" }
        }

        data class FullførtBehandlingForAnnenOmsorgsmottaker(
            val behandlingsId: UUID,
            val omsorgsyter: String,
            val omsorgsmottaker: String,
            val omsorgsår: Int,
            val erForelderTilOmsorgsmottaker: Boolean,
            val utfall: BehandlingUtfall
        )

        fun erManuell(): Boolean {
            return finnManuelle().isNotEmpty()
        }

        fun finnManuelle(): List<FullførtBehandlingForAnnenOmsorgsmottaker> {
            return behandlinger.filter { it.omsorgsår == omsorgsår && it.erForelderTilOmsorgsmottaker && (it.utfall.erInnvilget() || it.utfall.erManuell()) }
        }
    }
}
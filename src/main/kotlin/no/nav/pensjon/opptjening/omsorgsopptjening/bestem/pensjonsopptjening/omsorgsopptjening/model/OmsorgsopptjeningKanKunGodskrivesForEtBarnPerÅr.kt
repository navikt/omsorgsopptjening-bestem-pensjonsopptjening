package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

object OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr :
    ParagrafVilkår<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Første_Ledd_Innledning
        ).let { referanser ->
            if (grunnlag.behandlinger.none { it.erInnvilget && grunnlag.omsorgsår == it.omsorgsÅr }) {
                VilkårsvurderingUtfall.Innvilget.Vilkår(referanser)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår(referanser)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsmottaker: String,
        val omsorgsår: Int,
        val behandlinger: List<FullførtBehandlingForOmsorgsyter>
    ) : ParagrafGrunnlag() {
        data class FullførtBehandlingForOmsorgsyter(
            val behandlingsId: UUID,
            val omsorgsyter: String,
            val omsorgsmottaker: String,
            val omsorgsÅr: Int,
            val erInnvilget: Boolean
        )
    }
}
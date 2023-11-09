package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

object OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr :
    ParagrafVilkår<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return Vurdering(
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        setOf(
            Referanse.OmsorgsopptjeningGisKunEnOmsorgsyterPerKalenderÅr,
        ).let { referanser ->
            return if (grunnlag.fullførteBehandlinger.none { it.erInnvilget && grunnlag.omsorgsår == it.omsorgsår }) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(referanser)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(referanser)
            }
        }
    }

    data class Vurdering(
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsår: Int,
        val fullførteBehandlinger: List<FullførtBehandlingForOmsorgsmottaker>
    ) : ParagrafGrunnlag() {
        data class FullførtBehandlingForOmsorgsmottaker(
            val behandlingsId: UUID,
            val omsorgsyter: String,
            val omsorgsmottaker: String,
            val omsorgsår: Int,
            val erInnvilget: Boolean
        )
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

object OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr :
    ParagrafVilkår<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.`OpptjeningKanGodskrivesMed18,1ProsentAv4,5GHvertKalenderår`
        ).let { referanser ->
            if (grunnlag.behandlinger.none { it.erInnvilget && grunnlag.omsorgsår == it.omsorgsÅr }) {
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


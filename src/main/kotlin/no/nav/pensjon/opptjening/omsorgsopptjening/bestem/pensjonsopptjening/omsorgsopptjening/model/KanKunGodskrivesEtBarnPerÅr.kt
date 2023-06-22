package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

object KanKunGodskrivesEtBarnPerÅr : ParagrafVilkår<KanKunGodskrivesEtBarnPerÅr.Grunnlag>() {
    override fun vilkarsVurder(grunnlag: Grunnlag): Vurdering {
        return bestemUtfall(grunnlag).let {
            Vurdering(
                henvisninger = it.henvisninger(),
                grunnlag = grunnlag,
                utfall = it,
            )
        }
    }

    override fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall {
        return setOf(
            Referanse.OpptjeningKanKunGodskrivesForEtBarnPerÅr()
        ).let {
            if (grunnlag.behandlinger.none { it.erInnvilget }) {
                VilkårsvurderingUtfall.Innvilget.Vilkår.from(it)
            } else {
                VilkårsvurderingUtfall.Avslag.Vilkår.from(it)
            }
        }
    }

    data class Vurdering(
        override val henvisninger: Set<Henvisning>,
        override val grunnlag: Grunnlag,
        override val utfall: VilkårsvurderingUtfall
    ) : ParagrafVurdering<Grunnlag>()

    data class Grunnlag(
        val omsorgsmottaker: String,
        val behandlinger: List<AndreBehandlinger>
    ) : ParagrafGrunnlag() {
        data class AndreBehandlinger(
            val behandlingsId: UUID,
            val år: Int,
            val omsorgsmottaker: String,
            val erInnvilget: Boolean
        )
    }
}


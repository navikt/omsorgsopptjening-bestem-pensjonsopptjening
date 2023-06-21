package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEtBarnPerÅr : ParagrafVilkår<KanKunGodskrivesEtBarnPerÅrGrunnlag>() {
    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): KanKunGodskrivesEtBarnPerÅrVurdering {
        return bestemUtfall(grunnlag).let { KanKunGodskrivesEtBarnPerÅrVurdering(
            henvisninger = it.henvisninger(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
    }

    override fun <T : Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>> T.bestemUtfall(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): VilkårsvurderingUtfall {
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
}

data class KanKunGodskrivesEtBarnPerÅrVurdering(
    override val henvisninger: Set<Henvisning>,
    override val grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<KanKunGodskrivesEtBarnPerÅrGrunnlag>()

data class KanKunGodskrivesEtBarnPerÅrGrunnlag(
    val omsorgsmottaker: String,
    val behandlinger: List<AndreBehandlinger>
): ParagrafGrunnlag()

data class AndreBehandlinger(
    val behandlingsId: UUID,
    val år: Int,
    val omsorgsmottaker: String,
    val erInnvilget: Boolean
)
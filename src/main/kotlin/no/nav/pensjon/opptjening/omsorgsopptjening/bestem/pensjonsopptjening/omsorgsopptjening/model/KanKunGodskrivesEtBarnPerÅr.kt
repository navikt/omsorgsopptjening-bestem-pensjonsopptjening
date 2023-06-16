package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEtBarnPerÅr : ParagrafVilkår<KanKunGodskrivesEtBarnPerÅrGrunnlag>() {
    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): KanKunGodskrivesEtBarnPerÅrVurdering {
        return bestemUtfall(grunnlag).let { KanKunGodskrivesEtBarnPerÅrVurdering(
            lovhenvisninger = it.lovhenvisning(),
            grunnlag = grunnlag,
            utfall = it,
        ) }
    }

    override fun <T : Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>> T.bestemUtfall(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): VilkårsvurderingUtfall {
        val lovhenvisning = setOf(Lovhenvisning.MINST_HALVT_AR_OMSORG)
        return if (grunnlag.behandlinger.none { it.erInnvilget }) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(lovhenvisning = lovhenvisning)
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(lovhenvisning = lovhenvisning)
        }
    }
}

data class KanKunGodskrivesEtBarnPerÅrVurdering(
    override val lovhenvisninger: Set<Lovhenvisning>,
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
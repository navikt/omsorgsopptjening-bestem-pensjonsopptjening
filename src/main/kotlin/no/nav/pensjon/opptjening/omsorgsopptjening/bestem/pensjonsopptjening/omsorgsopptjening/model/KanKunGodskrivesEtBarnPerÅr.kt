package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEtBarnPerÅr : ParagrafVilkår<KanKunGodskrivesEtBarnPerÅrGrunnlag>(
    paragrafer = setOf(Paragraf.A),
) {
    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): KanKunGodskrivesEtBarnPerÅrVurdering {
        return KanKunGodskrivesEtBarnPerÅrVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = bestemUtfall(grunnlag),
        )
    }

    override fun <T : Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>> T.bestemUtfall(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): VilkårsvurderingUtfall {
        return if (grunnlag.behandlinger.none { it.erInnvilget }) {
            VilkårsvurderingUtfall.Innvilget.EnkeltParagraf(paragraf = paragrafer.single())
        } else {
            VilkårsvurderingUtfall.Avslag.EnkeltParagraf(paragraf = paragrafer.single())
        }
    }
}

data class KanKunGodskrivesEtBarnPerÅrVurdering(
    override val paragrafer: Set<Paragraf>,
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
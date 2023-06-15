package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEtBarnPerÅr : ParagrafVilkår<KanKunGodskrivesEtBarnPerÅrGrunnlag>(
    paragrafer = setOf(Paragraf.A),
    utfallsFunksjon = vurderUtfall as Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>.(KanKunGodskrivesEtBarnPerÅrGrunnlag) -> VilkårsvurderingUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun ParagrafVilkår<KanKunGodskrivesEtBarnPerÅrGrunnlag>.(input: KanKunGodskrivesEtBarnPerÅrGrunnlag): VilkårsvurderingUtfall {
                if (input.behandlinger.none { it.erInnvilget }) {
                    return KanKunGodskrivesEtBarnPerÅrInnvilget("")
                } else {
                    return KanKunGodskrivesEtBarnPerÅrAvslag(listOf(AvslagÅrsak.ALLEREDE_GODSKREVET_BARN_FOR_ÅR))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): KanKunGodskrivesEtBarnPerÅrVurdering {
        return KanKunGodskrivesEtBarnPerÅrVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class KanKunGodskrivesEtBarnPerÅrVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<KanKunGodskrivesEtBarnPerÅrGrunnlag>()

data class KanKunGodskrivesEtBarnPerÅrInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class KanKunGodskrivesEtBarnPerÅrAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()

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
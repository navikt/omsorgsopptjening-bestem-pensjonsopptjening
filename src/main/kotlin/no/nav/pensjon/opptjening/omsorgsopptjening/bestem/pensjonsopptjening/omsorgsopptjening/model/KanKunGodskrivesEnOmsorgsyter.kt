package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import java.util.UUID

class KanKunGodskrivesEnOmsorgsyter : ParagrafVilkår<KanKunGodskrivesEnOmsorgsyterGrunnlag>(
    paragrafer = setOf(Paragraf.A),
    utfallsFunksjon = vurderUtfall as Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>.(KanKunGodskrivesEnOmsorgsyterGrunnlag) -> VilkårsvurderingUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun ParagrafVilkår<KanKunGodskrivesEnOmsorgsyterGrunnlag>.(input: KanKunGodskrivesEnOmsorgsyterGrunnlag): VilkårsvurderingUtfall {
                if (input.behandlingsIdUtfallListe.none { it.erInnvilget }) {
                    return KanKunGodskrivesEnOmsorgsyterInnvilget("")
                } else {
                    return KanKunGodskrivesEnOmsorgsyterAvslag(listOf(AvslagÅrsak.ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): KanKunGodskrivesEnOmsorgsyterVurdering {
        return KanKunGodskrivesEnOmsorgsyterVurdering(
            paragrafer = paragrafer,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class KanKunGodskrivesEnOmsorgsyterVurdering(
    override val paragrafer: Set<Paragraf>,
    override val grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : ParagrafVurdering<KanKunGodskrivesEnOmsorgsyterGrunnlag>()

data class KanKunGodskrivesEnOmsorgsyterInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class KanKunGodskrivesEnOmsorgsyterAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()

data class KanKunGodskrivesEnOmsorgsyterGrunnlag(
    val behandlingsIdUtfallListe: List<BehandlingsIdUtfall>
): ParagrafGrunnlag()

data class BehandlingsIdUtfall(
    val behandlingsId: UUID,
    val erInnvilget: Boolean
)
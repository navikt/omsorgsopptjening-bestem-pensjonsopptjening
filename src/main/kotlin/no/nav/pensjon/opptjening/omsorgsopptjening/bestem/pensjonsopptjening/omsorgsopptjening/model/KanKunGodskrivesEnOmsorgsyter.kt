package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class KanKunGodskrivesEnOmsorgsyter : Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Det kan gis pensjonsopptjening etter første ledd fra og med det året vedkommende fyller 17 år.",
        begrunnesleForAvslag = "Omsorgsmottaker har vært grunnlag for godskriving av annen omsorgsyter.",
        begrunnelseForInnvilgelse = "Omsorgsmottaker har ikke vært grunnlag for godskriving av annen omsorgsyter.",
    ),
    utfallsFunksjon = vurderUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>.(input: KanKunGodskrivesEnOmsorgsyterGrunnlag): VilkårsvurderingUtfall {
                if (input.behandlingsIdUtfallListe.none { it.erInnvilget }) {
                    return KanKunGodskrivesEnOmsorgsyterInnvilget(vilkarsInformasjon.begrunnelseForInnvilgelse)
                } else {
                    return KanKunGodskrivesEnOmsorgsyterAvslag(listOf(AvslagÅrsak.ALLEREDE_INNVILGET_FOR_ANNEN_MOTTAKER))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag): KanKunGodskrivesEnOmsorgsyterVurdering {
        return KanKunGodskrivesEnOmsorgsyterVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class KanKunGodskrivesEnOmsorgsyterVurdering(
    override val vilkar: Vilkar<KanKunGodskrivesEnOmsorgsyterGrunnlag>,
    override val grunnlag: KanKunGodskrivesEnOmsorgsyterGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<KanKunGodskrivesEnOmsorgsyterGrunnlag>()

data class KanKunGodskrivesEnOmsorgsyterInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class KanKunGodskrivesEnOmsorgsyterAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()

data class KanKunGodskrivesEnOmsorgsyterGrunnlag(
    val behandlingsIdUtfallListe: List<BehandlingsIdUtfall>
)

data class BehandlingsIdUtfall(
    val behandlingsId: Long,
    val erInnvilget: Boolean
)
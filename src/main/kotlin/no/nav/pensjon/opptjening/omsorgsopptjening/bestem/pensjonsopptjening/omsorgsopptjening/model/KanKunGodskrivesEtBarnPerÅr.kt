package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

class KanKunGodskrivesEtBarnPerÅr : Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>(
    vilkarsInformasjon = VilkarsInformasjon(
        beskrivelse = "Kan kun godskrives et barn per år",
        begrunnesleForAvslag = "bla b",
        begrunnelseForInnvilgelse = "bla",
    ),
    utfallsFunksjon = vurderUtfall,
) {
    companion object {
        private val vurderUtfall =
            fun Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>.(input: KanKunGodskrivesEtBarnPerÅrGrunnlag): VilkårsvurderingUtfall {
                if (input.behandlinger.none { it.erInnvilget }) {
                    return KanKunGodskrivesEtBarnPerÅrInnvilget(vilkarsInformasjon.begrunnelseForInnvilgelse)
                } else {
                    return KanKunGodskrivesEtBarnPerÅrAvslag(listOf(AvslagÅrsak.ALLEREDE_GODSKREVET_BARN_FOR_ÅR))
                }
            }
    }

    override fun vilkarsVurder(grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag): KanKunGodskrivesEtBarnPerÅrVurdering {
        return KanKunGodskrivesEtBarnPerÅrVurdering(
            vilkar = this,
            grunnlag = grunnlag,
            utfall = utfallsFunksjon(grunnlag),
        )
    }
}

data class KanKunGodskrivesEtBarnPerÅrVurdering(
    override val vilkar: Vilkar<KanKunGodskrivesEtBarnPerÅrGrunnlag>,
    override val grunnlag: KanKunGodskrivesEtBarnPerÅrGrunnlag,
    override val utfall: VilkårsvurderingUtfall
) : VilkarsVurdering<KanKunGodskrivesEtBarnPerÅrGrunnlag>()

data class KanKunGodskrivesEtBarnPerÅrInnvilget(val årsak: String) : VilkårsvurderingUtfall.Innvilget()
data class KanKunGodskrivesEtBarnPerÅrAvslag(override val årsaker: List<AvslagÅrsak>) :
    VilkårsvurderingUtfall.Avslag()

data class KanKunGodskrivesEtBarnPerÅrGrunnlag(
    val omsorgsmottaker: String,
    val behandlinger: List<AndreBehandlinger>
)

data class AndreBehandlinger(
    val behandlingsId: Long,
    val år: Int,
    val omsorgsmottaker: String,
    val erInnvilget: Boolean
)
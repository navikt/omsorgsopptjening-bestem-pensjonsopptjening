package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class AggregertBehandlingsutfall(
    private val alle: List<BehandlingUtfall>
) {
    fun utfall(): BehandlingUtfall {
        return when {
            alle.any { it.erInnvilget() } -> {
                BehandlingUtfall.Innvilget
            }

            alle.none { it.erInnvilget() } && alle.any { it.erManuell() } -> {
                BehandlingUtfall.Manuell
            }

            else -> {
                BehandlingUtfall.Avslag
            }
        }
    }
}
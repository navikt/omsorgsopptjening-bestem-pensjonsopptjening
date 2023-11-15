package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class AggregertBehandlingsutfall(
    private val alle: List<BehandlingUtfall>
) {
    fun utfall(): AggregertBehandlingUtfall {
        return when {
            alle.any { it.erInnvilget() } -> {
                AggregertBehandlingUtfall.Innvilget
            }

            alle.none { it.erInnvilget() } && alle.any { it.erManuell() } -> {
                AggregertBehandlingUtfall.Manuell
            }

            else -> {
                AggregertBehandlingUtfall.Avslag
            }
        }
    }
}
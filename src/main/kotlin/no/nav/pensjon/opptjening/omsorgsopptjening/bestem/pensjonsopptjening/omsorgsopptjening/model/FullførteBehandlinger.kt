package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class FullførteBehandlinger(
    private val behandlinger: List<FullførtBehandling>
) {
    private val behandlingerPerOmsorgsår = behandlinger
        .alleOmsorgsår()
        .associateWith { år -> behandlinger.filter { år == it.omsorgsAr } }
        .map { (år, behandlinger) ->
            Triple(
                first = år,
                second = behandlinger,
                third = AggregerBehandlingsutfallPerOmsorgsår(behandlinger).utfall()
            )
        }

    init {
        require(behandlingerPerOmsorgsår.flatMap { it.second }.map { it.utfall }
                    .count { it.erInnvilget() } <= 1) { "Det kan kun eksistere 0..1 innvilget behandling per omsorgsyter per år." }
    }

    fun alle(): List<FullførtBehandling> {
        return behandlinger
    }

    private fun innvilget(år: Int): FullførtBehandling? {
        return finnÅr(år)?.second?.singleOrNull { it.erInnvilget() }
    }

    private fun manuell(år: Int): List<FullførtBehandling> {
        return finnÅr(år)?.second?.filter { it.erManuell() } ?: emptyList()
    }

    fun håndterUtfall(
        innvilget: (behandling: FullførtBehandling) -> Unit,
        manuell: (behandling: FullførtBehandling) -> Unit,
        avslag: () -> Unit,
    ) {
        behandlingerPerOmsorgsår.forEach { (år, _, utfall) ->
            when (utfall) {
                BehandlingUtfall.Avslag -> {
                    avslag()
                }

                BehandlingUtfall.Innvilget -> {
                    innvilget(innvilget(år)!!)
                }

                BehandlingUtfall.Manuell -> {
                    manuell(år).forEach { manuell(it) }
                }
            }
        }
    }


    private fun finnÅr(år: Int) = behandlingerPerOmsorgsår.singleOrNull { it.first == år }

    private fun List<FullførtBehandling>.alleOmsorgsår(): Set<Int> {
        return this.map { it.omsorgsAr }.toSet()
    }
}

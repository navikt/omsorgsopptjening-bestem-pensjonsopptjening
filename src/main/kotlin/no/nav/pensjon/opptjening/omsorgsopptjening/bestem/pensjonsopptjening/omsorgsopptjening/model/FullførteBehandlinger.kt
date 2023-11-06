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

    fun perÅr(): List<Triple<Int, List<FullførtBehandling>, BehandlingUtfall>> {
        return behandlingerPerOmsorgsår
    }

    fun forAlleÅr(): List<FullførtBehandling> {
        return behandlinger
    }

    private fun List<FullførtBehandling>.alleOmsorgsår(): Set<Int> {
        return this.map { it.omsorgsAr }.toSet()
    }
}

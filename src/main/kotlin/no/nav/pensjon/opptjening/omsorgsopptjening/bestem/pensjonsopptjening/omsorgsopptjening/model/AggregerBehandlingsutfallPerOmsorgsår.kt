package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class AggregerBehandlingsutfallPerOmsorgsår(
    private val alle: List<FullførtBehandling>
) {
    init {
        require(
            alle.map { it.omsorgsAr }.distinct().count() == 1
        ) { "Kan ikke aggregere utfall på tvers av omsorgsår" }
        require(
            alle.map { it.omsorgsyter }.distinct().count() == 1
        ) { "Kan ikke aggregere utfall på tvers av omsorgsytere" }
    }

    fun utfall(): BehandlingUtfall {
        return AggregertBehandlingsutfall(alle.map { it.utfall }).utfall()
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class AvgjørBehandlingUtfall(
    private val vilkårsvurdering: VilkarsVurdering<*>
) {
    fun utfall(): BehandlingUtfall {
        return when {
            vilkårsvurdering.erInnvilget() -> {
                BehandlingUtfall.Innvilget
            }

            vilkårsvurdering.erUbestemt() -> {
                BehandlingUtfall.Manuell
            }

            else -> {
                BehandlingUtfall.Avslag
            }
        }
    }
}
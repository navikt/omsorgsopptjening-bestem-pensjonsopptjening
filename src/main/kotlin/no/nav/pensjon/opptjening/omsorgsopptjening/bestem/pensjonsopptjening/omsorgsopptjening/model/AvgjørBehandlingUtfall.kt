package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

data class AvgjørBehandlingUtfall(
    private val vilkårsvurdering: VilkarsVurdering<*>
){
    private val innvilget = vilkårsvurdering.finnAlleInnvilget()
    private val avslag = vilkårsvurdering.finnAlleAvslatte()
    private val ubestemt = vilkårsvurdering.finnAlleUbestemte()

    fun utfall(): BehandlingUtfall {
        return when {
            innvilget.isNotEmpty() && avslag.isEmpty() && ubestemt.isEmpty() -> {
                BehandlingUtfall.Innvilget
            }
            innvilget.isNotEmpty() && avslag.isEmpty() && ubestemt.isNotEmpty() -> {
                BehandlingUtfall.Manuell
            }
            innvilget.isEmpty() && avslag.isEmpty() && ubestemt.isNotEmpty() -> {
                BehandlingUtfall.Manuell
            }
            else -> {
                BehandlingUtfall.Avslag
            }
        }
    }
}
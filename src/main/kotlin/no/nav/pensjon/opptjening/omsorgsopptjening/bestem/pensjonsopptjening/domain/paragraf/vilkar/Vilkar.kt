package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar


//TODO make sexy
open class Vilkar<T : Any>(
    private val vilkarsInformasjon: VilkarsInformasjon,
    private val oppfyllerRegler: (T) -> Boolean,
) {
    fun inputTilVilkarsvurdering(inputVerdi: T) = VilkarsVurdering(
        vilkarsInformasjon = vilkarsInformasjon,
        oppfyllerRegler = oppfyllerRegler,
        inputVerdi = inputVerdi
    )
}

//TODO reneame parameter to something more relevant for the domain
open class VilkarsVurdering<T : Any>(
    private val vilkarsInformasjon: VilkarsInformasjon,
    private val oppfyllerRegler: (T) -> Boolean,
    private val inputVerdi: T
) {
    open fun utførVilkarsVurdering(): VilkarsResultat {
        val oppfyllerRegel = oppfyllerRegler(inputVerdi)
        return VilkarsResultat(
            input = inputVerdi,
            beskrivelse = vilkarsInformasjon.beskrivelse,
            oppFyllerRegel = oppfyllerRegel,
            begrunnelseForAvgjørelse = if (oppfyllerRegel) vilkarsInformasjon.begrunnelseForInnvilgelse else vilkarsInformasjon.begrunnesleForAvslag
        )
    }
}

data class VilkarsInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)

data class VilkarsResultat(
    val brukteResultat: List<Result<Any>> = listOf(),
    val beskrivelse: String,
    val oppFyllerRegel: Boolean,
    val begrunnelseForAvgjørelse: String,
    val input: Any,
)




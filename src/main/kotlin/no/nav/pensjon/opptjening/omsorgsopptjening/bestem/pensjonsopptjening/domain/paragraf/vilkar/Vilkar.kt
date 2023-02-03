package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar


//TODO make sexy
open class Vilkar<T : Any>(
    private val regelInformasjon: RegelInformasjon,
    private val oppfyllerRegler: (T) -> Boolean,
) {
    fun medInput(inputVerdi: T) = VilkarsVurdering(
        regelInformasjon = regelInformasjon,
        oppfyllerRegler = oppfyllerRegler,
        inputVerdi = inputVerdi
    )
}

//TODO reneame parameter to something more relevant for the domain
open class VilkarsVurdering<T : Any>(
    private val regelInformasjon: RegelInformasjon,
    private val oppfyllerRegler: (T) -> Boolean,
    private val inputVerdi: T
) {
    open fun utforRegel(): RegelResultat {
        val oppfyllerRegel = oppfyllerRegler(inputVerdi)
        return RegelResultat(
            input = inputVerdi,
            beskrivelse = regelInformasjon.beskrivelse,
            oppFyllerRegel = oppfyllerRegel,
            begrunnelseForAvgjørelse = if (oppfyllerRegel) regelInformasjon.begrunnelseForInnvilgelse else regelInformasjon.begrunnesleForAvslag
        )
    }
}



data class RegelInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)

data class RegelResultat(
    val brukteResultat: List<Result<Any>> = listOf(),
    val beskrivelse: String,
    val oppFyllerRegel: Boolean,
    val begrunnelseForAvgjørelse: String,
    val input: Any,
)




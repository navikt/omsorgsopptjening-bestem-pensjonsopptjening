package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.regel

//TODO reneame parameter to something more relevant for the domain
open class RegelInstanse<T : Any>(
    private val regelInformasjon: RegelInformasjon,
    private val oppfyllerRegler: (T) -> Boolean,
    private val inputVerdi: T
) {
    open fun bruk(): RegelResultat {
        val oppfyllerRegel = oppfyllerRegler(inputVerdi)
        return RegelResultat(
            input = inputVerdi,
            beskrivelse = regelInformasjon.beskrivelse,
            oppFyllerRegel = oppfyllerRegel,
            begrunnelseForAvgjørelse = if (oppfyllerRegel) regelInformasjon.begrunnelseForInnvilgelse else regelInformasjon.begrunnesleForAvslag
        )
    }
}

//TODO make sexy
open class Regel<T : Any>(
    private val regelInformasjon: RegelInformasjon,
    private val oppfyllerRegler: (T) -> Boolean,
) {

    fun lagRegelInstanse(inputVerdi: T) = RegelInstanse(
        regelInformasjon = regelInformasjon,
        oppfyllerRegler = oppfyllerRegler,
        inputVerdi = inputVerdi
    )
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




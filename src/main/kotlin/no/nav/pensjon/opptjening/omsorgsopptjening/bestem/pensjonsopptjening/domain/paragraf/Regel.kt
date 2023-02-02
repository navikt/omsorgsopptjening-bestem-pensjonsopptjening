package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf

open class Regel<T : Any>(
    private val regelInformasjon: RegelInformasjon,
    val inputVerdi: T,
    private val oppfyllerRegler : (T) -> Boolean
) {

    open fun brukRegel(): RegelResultat {
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




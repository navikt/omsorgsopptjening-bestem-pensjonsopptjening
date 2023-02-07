package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar


open class Vilkar<InputType : Any>(
    val regelInformasjon: RegelInformasjon,
    val oppfyllerRegler: (InputType) -> Boolean,
) {
    fun lagVilkarsVurdering(inputVerdi: InputType) = VilkarsVurdering(
        vilkar = this,
        inputVerdi = inputVerdi
    )

    fun utførVilkarsVurdering(input: InputType) =  VilkarsVurdering(this, input).utførVilkarsVurdering()
}

data class RegelInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)

open class VilkarsVurdering<InputType : Any>(
    private val vilkar: Vilkar<InputType>,
    private val inputVerdi: InputType
) {
    open fun utførVilkarsVurdering(): VilkarsResultat<InputType> {
        return VilkarsResultat(
            oppFyllerRegel = vilkar.oppfyllerRegler(inputVerdi),
            inputVerdi = inputVerdi,
            vilkar = vilkar
        )
    }
}

data class VilkarsResultat<InputType: Any>(
    val oppFyllerRegel: Boolean,
    val inputVerdi: InputType,
    val vilkar : Vilkar<InputType>,
)

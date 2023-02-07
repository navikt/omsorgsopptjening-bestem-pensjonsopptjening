package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.paragraf.vilkar

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
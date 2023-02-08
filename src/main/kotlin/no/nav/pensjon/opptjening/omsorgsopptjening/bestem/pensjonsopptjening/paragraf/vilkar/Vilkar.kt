package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar


open class Vilkar<Input : Any>(
    val regelInformasjon: RegelInformasjon,
    val oppfyllerRegler: (Input) -> Boolean,
) {
    fun lagVilkarsVurdering(inputVerdi: Input) = VilkarsVurdering(
        vilkar = this,
        inputVerdi = inputVerdi
    )

    fun utførVilkarsVurdering(input: Input) =  VilkarsVurdering(this, input).utførVilkarsVurdering()
}

data class RegelInformasjon(
    val beskrivelse: String,
    val begrunnesleForAvslag: String,
    val begrunnelseForInnvilgelse: String,
)
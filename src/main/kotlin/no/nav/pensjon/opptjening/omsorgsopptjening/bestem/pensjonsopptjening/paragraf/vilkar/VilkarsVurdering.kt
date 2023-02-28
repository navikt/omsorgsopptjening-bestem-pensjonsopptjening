package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

open class VilkarsVurdering<Grunnlag : Any>(
    private val vilkar: Vilkar<Grunnlag>,
    private val grunnlag: Grunnlag
) {

    open fun utførVilkarsVurdering(): VilkarsResultat<Grunnlag> {
        return VilkarsResultat(
            oppFyllerRegel = vilkar.oppfyllerRegler(grunnlag),
            grunnlag = grunnlag,
            vilkar = vilkar
        )
    }

}

data class VilkarsResultat<Grunnlag : Any>(
    val oppFyllerRegel: Boolean,
    val grunnlag: Grunnlag,
    val vilkar: Vilkar<Grunnlag>,
)
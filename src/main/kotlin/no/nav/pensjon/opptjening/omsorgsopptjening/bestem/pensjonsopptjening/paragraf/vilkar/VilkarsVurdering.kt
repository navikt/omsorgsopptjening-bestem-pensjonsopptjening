package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

open class VilkarsVurdering<Grunnlag : Any>(
    val vilkar: Vilkar<Grunnlag>,
    val grunnlag: Grunnlag
) {

    open fun utfor(): VilkarsResultat<Grunnlag> {
        return VilkarsResultat(
            avgjorelse = vilkar.avgjorelsesFunksjon(grunnlag),
            grunnlag = grunnlag,
            vilkar = vilkar
        )
    }
}
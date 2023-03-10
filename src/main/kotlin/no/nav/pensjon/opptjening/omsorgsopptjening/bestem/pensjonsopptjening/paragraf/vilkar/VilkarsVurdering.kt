package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

open class VilkarsVurdering<Grunnlag : Any>(
    private val vilkar: Vilkar<Grunnlag>,
    private val grunnlag: Grunnlag
) {

    open fun utforVilkarsVurdering(): VilkarsResultat<Grunnlag> {
        return VilkarsResultat(
            avgjorelse = vilkar.kalkulerAvgjorelse(grunnlag),
            grunnlag = grunnlag,
            vilkar = vilkar
        )
    }
}
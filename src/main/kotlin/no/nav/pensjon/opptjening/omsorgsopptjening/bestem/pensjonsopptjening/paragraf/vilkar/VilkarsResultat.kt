package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

data class VilkarsResultat<Grunnlag : Any>(
    val avgjorelse: Avgjorelse,
    val grunnlag: Grunnlag,
    val vilkar: Vilkar<Grunnlag>,
) {
    fun accept(vilkarsResultatVisitor: VilkarsResultatVisitor) {
        vilkarsResultatVisitor.visit(this)
        if(grunnlag is VilkarsResultat<*>) grunnlag.accept(vilkarsResultatVisitor)
    }
}

enum class Avgjorelse {
    INVILGET,
    AVSLAG,
    SAKSBEHANDLING
}
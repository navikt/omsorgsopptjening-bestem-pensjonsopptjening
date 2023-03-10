package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

data class VilkarsResultat<Grunnlag : Any>(
    val avgjorelse: Boolean,
    val grunnlag: Grunnlag,
    val vilkar: Vilkar<Grunnlag>,
)
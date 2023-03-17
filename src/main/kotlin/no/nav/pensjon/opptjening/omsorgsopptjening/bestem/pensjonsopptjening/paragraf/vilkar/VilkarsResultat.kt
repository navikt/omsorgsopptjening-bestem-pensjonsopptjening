package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

data class VilkarsResultat(
    val avgjorelse: Avgjorelse,
    val vilkarsVurdering: VilkarsVurdering<*>,
)

enum class Avgjorelse {
    INVILGET,
    AVSLAG,
    SAKSBEHANDLING,
}
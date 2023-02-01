package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.vilkarsvurdering

class Vilkorsvurdering(private val vurderinger: List<Vilkar>) {

    fun erIkkeInnvilget() = vurderinger
        .map { vilkar -> vilkar.vurder() }
        .map { it.oppfyllerVilkar }
        .find { !it }

}

interface Vilkar {

    fun vurder(): VilkarsResultat

    fun lovText(): String

    fun begrunnesleForAvslag(): String

    fun begrunnelseForInnvilgelse() : String

}

data class VilkarsResultat(
    val lovText: String,
    val oppfyllerVilkar: Boolean,
    val begrunnelse: String,
)




package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class OmsorgsArbeid internal constructor(private val omsorgsYter: List<OmsorgsYter> = listOf()) {
    fun monthsOfOmsorg(): Int = 4
}

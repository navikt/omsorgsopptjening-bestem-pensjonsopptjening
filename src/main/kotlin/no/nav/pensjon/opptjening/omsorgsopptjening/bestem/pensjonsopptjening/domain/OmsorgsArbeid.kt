package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class OmsorgsArbeid internal constructor(
    private val omsorgsYter: Omsorgsyter,
    private val omsorgsAr: Int

    ) {
    fun monthsOfOmsorg() = omsorgsYter.monthsOfOmsorg(omsorgsAr)
}

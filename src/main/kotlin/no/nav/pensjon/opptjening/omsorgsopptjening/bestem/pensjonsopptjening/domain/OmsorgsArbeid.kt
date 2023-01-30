package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class OmsorgsArbeid internal constructor(
    private val omsorgsYter: Omsorgsyter,
) {
    fun monthsWithOmsorg(omsorgsAr: Int) = omsorgsYter.monthsWithOmsorgsarbeid(omsorgsAr)
}

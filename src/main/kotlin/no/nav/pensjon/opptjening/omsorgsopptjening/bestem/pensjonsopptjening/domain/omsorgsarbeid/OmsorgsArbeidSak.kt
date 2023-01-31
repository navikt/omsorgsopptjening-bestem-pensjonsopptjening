package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

class OmsorgsArbeidSak internal constructor(
    private val utfortOmsorgsArbeid: UtfortOmsorgsarbeid,
) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int) = utfortOmsorgsArbeid.monthsWithOmsorgsarbeid(omsorgsAr)
}

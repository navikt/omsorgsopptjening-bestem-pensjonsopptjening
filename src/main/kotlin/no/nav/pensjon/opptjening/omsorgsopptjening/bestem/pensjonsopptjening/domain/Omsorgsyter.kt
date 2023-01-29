package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.Months.Companion.crateEmptyMonths

class Omsorgsyter(private val omsorgsArbeidPeriode: List<OmsorgsArbeidPeriode>) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int): Int = totalMonthsOfPayment().restrictToYear(omsorgsAr).countMonths()

    private fun totalMonthsOfPayment() = omsorgsArbeidPeriode
        .fold(initial = crateEmptyMonths()) { totalMonths, period -> totalMonths + period.monthsWithPayment }
}
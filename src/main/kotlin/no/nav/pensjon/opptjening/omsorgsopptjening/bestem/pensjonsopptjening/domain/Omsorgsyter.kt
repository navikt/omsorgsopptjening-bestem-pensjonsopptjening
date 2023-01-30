package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.UtbetalingMoneder.Companion.crateUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class Omsorgsyter(
    private val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalinger>,
    private val person: Person
) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int): Int = totalMonthsOfPayment().restrictToYear(omsorgsAr).countMonths()

    private fun totalMonthsOfPayment() = omsorgsArbeidsUtbetalinger
        .fold(initial = crateUtbetalingMoneder()) { accumulatedMonths, period -> accumulatedMonths + period.monthsWithPayment }
}
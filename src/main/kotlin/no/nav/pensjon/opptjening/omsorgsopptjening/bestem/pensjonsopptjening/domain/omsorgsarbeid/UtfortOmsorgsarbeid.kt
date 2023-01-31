package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.UtbetalingMoneder.Companion.crateUtbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class UtfortOmsorgsarbeid(
    private val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalinger>,
    private val person: Person
) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int): Int = totalMonthsOfPayment().restrictToYear(omsorgsAr).countMonths()

    infix fun isUtfortAvPerson(otherPerson: Person) = person isSamePerson otherPerson

    private fun totalMonthsOfPayment() = omsorgsArbeidsUtbetalinger
        .fold(initial = crateUtbetalingMoneder()) { accumulatedMonths, period -> accumulatedMonths + period.monthsWithPayment }
}
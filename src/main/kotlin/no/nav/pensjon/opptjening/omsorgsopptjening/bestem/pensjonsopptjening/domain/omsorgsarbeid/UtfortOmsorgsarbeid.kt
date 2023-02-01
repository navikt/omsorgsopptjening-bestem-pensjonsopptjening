package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class UtfortOmsorgsarbeid(
    private val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalinger>, private val person: Person
) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int): Int = (mondederMedUtbetalingerTotalt hentforAr omsorgsAr).monedCount()

    infix fun isUtfortAvPerson(otherPerson: Person) = person isSamePerson otherPerson

    private val mondederMedUtbetalingerTotalt get() =
        omsorgsArbeidsUtbetalinger
            .map { utbetalinger -> utbetalinger.utbetalingMoneder }
            .fold(utbetalingMoneder()) { acc, moneder -> acc + moneder }
}
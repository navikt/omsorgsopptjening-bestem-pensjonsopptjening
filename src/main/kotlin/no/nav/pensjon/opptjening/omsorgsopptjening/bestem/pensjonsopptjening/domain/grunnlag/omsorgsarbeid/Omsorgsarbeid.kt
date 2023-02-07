package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.grunnlag.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.grunnlag.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class Omsorgsarbeid(
    val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalinger>,
    val person: Person
) {
    infix fun mondederMedUtbetalinger(omsorgsAr: Int): Int = (mondederMedUtbetalingerTotalt begrensTilAr omsorgsAr).antall()

    infix fun erUtfortAv(annenPerson: Person) = annenPerson erSammePerson person

    private val mondederMedUtbetalingerTotalt
        get() =
            omsorgsArbeidsUtbetalinger
                .map { utbetalinger -> utbetalinger.utbetalingMoneder() }
                .fold(utbetalingMoneder()) { acc, moneder -> acc + moneder }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.UtbetalingMoneder.Companion.crateUtbetalingMoneder
import java.time.LocalDate

class OmsorgsArbeidsUtbetalinger(
    private val fom: LocalDate,
    private val tom: LocalDate
) {
    val monthsWithPayment get() = crateUtbetalingMoneder(fom,  tom)
}
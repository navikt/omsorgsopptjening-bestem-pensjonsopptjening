package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.UtbetalingMoneder.Companion.crateUtbetalingMoneder
import java.time.YearMonth

class OmsorgsArbeidsUtbetalinger(
    private val fom: YearMonth,
    private val tom: YearMonth
) {
    val monthsWithPayment get() = crateUtbetalingMoneder(fom,  tom)
}
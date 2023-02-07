package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.grunnlag.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.grunnlag.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalingMoneder
import java.time.YearMonth

class OmsorgsArbeidsUtbetalinger(
    private val fom: YearMonth,
    private val tom: YearMonth
) {
    val utbetalingMoneder get() = utbetalingMoneder(fom,  tom)
}
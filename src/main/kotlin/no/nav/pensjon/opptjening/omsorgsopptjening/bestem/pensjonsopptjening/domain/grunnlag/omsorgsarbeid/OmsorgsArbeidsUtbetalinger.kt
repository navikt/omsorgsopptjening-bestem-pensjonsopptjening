package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.grunnlag.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.grunnlag.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalingMoneder
import java.time.YearMonth

class OmsorgsArbeidsUtbetalinger(
    val fom: YearMonth,
    val tom: YearMonth
) {
    fun utbetalingMoneder() = utbetalingMoneder(fom,  tom)
}
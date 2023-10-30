package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.Month
import java.time.YearMonth

fun år(år: Int): Periode {
    return Periode(YearMonth.of(år, Month.JANUARY), YearMonth.of(år, Month.DECEMBER))
}
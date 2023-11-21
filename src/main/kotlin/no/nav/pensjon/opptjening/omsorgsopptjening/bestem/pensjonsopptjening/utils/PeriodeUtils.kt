package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import java.time.Month
import java.time.YearMonth

fun år(år: Int): Periode {
    return Periode(YearMonth.of(år, Month.JANUARY), YearMonth.of(år, Month.DECEMBER))
}

fun januar(år: Int) = YearMonth.of(år, Month.JANUARY)!!
fun februar(år: Int) = YearMonth.of(år, Month.FEBRUARY)!!
fun mars(år: Int) = YearMonth.of(år, Month.MARCH)!!
fun april(år: Int) = YearMonth.of(år, Month.APRIL)!!
fun mai(år: Int) = YearMonth.of(år, Month.MAY)!!
fun juni(år: Int) = YearMonth.of(år, Month.JUNE)!!
fun juli(år: Int) = YearMonth.of(år, Month.JULY)!!
fun august(år: Int) = YearMonth.of(år, Month.AUGUST)!!
fun september(år: Int) = YearMonth.of(år, Month.SEPTEMBER)!!
fun oktober(år: Int) = YearMonth.of(år, Month.OCTOBER)!!
fun november(år: Int) = YearMonth.of(år, Month.NOVEMBER)!!
fun desember(år: Int) = YearMonth.of(år, Month.DECEMBER)!!
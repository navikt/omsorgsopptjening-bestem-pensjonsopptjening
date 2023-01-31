package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.util.yearmonth.rangeTo
import java.time.YearMonth

class UtbetalingMoneder private constructor(private val months: Set<YearMonth> = setOf()) {

    fun countMonths(): Int = months.size

    infix fun restrictToYear(year: Int): UtbetalingMoneder = UtbetalingMoneder(months.filter { it.year == year }.toSet())

    operator fun plus(other: UtbetalingMoneder) = UtbetalingMoneder(this.months + other.months)

    companion object {
        fun crateUtbetalingMoneder(fom: YearMonth, tom: YearMonth) = UtbetalingMoneder((fom..tom).toSet())

        fun crateUtbetalingMoneder() = UtbetalingMoneder()
    }
}
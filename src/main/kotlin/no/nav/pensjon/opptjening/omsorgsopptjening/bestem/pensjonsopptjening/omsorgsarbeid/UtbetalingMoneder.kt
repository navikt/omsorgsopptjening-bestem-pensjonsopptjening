package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.yearmonth.rangeTo
import java.time.YearMonth

class UtbetalingMoneder private constructor(val months: Set<YearMonth> = setOf()) {

    fun antall(): Int = months.size

    infix fun begrensTilAr(ar: Int): UtbetalingMoneder = UtbetalingMoneder(months.filter { it.year == ar }.toSet())

    operator fun plus(other: UtbetalingMoneder) = UtbetalingMoneder(this.months + other.months)

    companion object {
        fun UtbetalingMoneder(fom: YearMonth, tom: YearMonth) = UtbetalingMoneder((fom..tom).toSet())

        fun emptyUtbetalingMoneder() = UtbetalingMoneder()
    }
}

package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.util.yearmonth.rangeTo
import java.time.YearMonth

class UtbetalingMoneder private constructor(private val months: Set<YearMonth> = setOf()) {

    fun monedCount(): Int = months.size

    infix fun innenforAr(ar: Int): UtbetalingMoneder = UtbetalingMoneder(months.filter { it.year == ar }.toSet())

    operator fun plus(other: UtbetalingMoneder) = UtbetalingMoneder(this.months + other.months)

    companion object {
        fun utbetalingMoneder(fom: YearMonth, tom: YearMonth) = UtbetalingMoneder((fom..tom).toSet())

        fun utbetalingMoneder() = UtbetalingMoneder()
    }
}
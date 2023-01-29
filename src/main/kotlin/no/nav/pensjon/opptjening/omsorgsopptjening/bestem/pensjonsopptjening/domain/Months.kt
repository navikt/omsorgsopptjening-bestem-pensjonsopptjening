package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.util.yearmonth.rangeTo
import java.time.LocalDate
import java.time.YearMonth

class Months private constructor(private val months: Set<YearMonth> = setOf()) {

    fun countMonths(): Int = months.size

    infix fun restrictToYear(year: Int): Months = Months(months.filter { it.year == year }.toSet())

    operator fun plus(other: Months) = Months(this.months + other.months)

    companion object {
        fun crateMonths(fom: LocalDate, tom: LocalDate) = Months((YearMonth.from(fom)..YearMonth.from(tom)).toSet())

        fun crateEmptyMonths() = Months()
    }
}
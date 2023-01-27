package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

internal class Periode(private val months: List<YearMonth>) {

    infix fun count(unit: TimeUnit): Long = months.size + 0L

    infix fun restrictTo(year: Int): Periode = Periode(months.filter{it.year == year})
}

enum class TimeUnit { MONTHS }


internal infix fun LocalDate.periode(to: LocalDate): Periode {
    val months = ArrayList<YearMonth>()
    var lastYearMonth = YearMonth.of(this.year,this.month)
    while (lastYearMonth <= YearMonth.of(to.year, to.month)){
        months.add(lastYearMonth)
        lastYearMonth = lastYearMonth.plusMonths(1)
    }
    return Periode(months)
}
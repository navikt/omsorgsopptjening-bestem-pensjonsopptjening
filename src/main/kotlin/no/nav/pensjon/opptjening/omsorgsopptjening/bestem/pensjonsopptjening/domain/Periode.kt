package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit


internal class Periode(val fom: YearMonth, val tom: YearMonth) {

    infix fun count(unit: TimeUnit): Long {
        return when {
            fom == UNDEFINED_FOM || tom == UNDEFINED_TOM -> 0
            else -> ChronoUnit.MONTHS.between(fom, tom.plusMonths(1))
        }
    }

    infix fun restrictTo(year: Int): Periode {
        val restrictedFom = when {
            fom isBeforeFirstMonthOf year -> year.firstOf()
            fom isAfterLastMonthOf year -> UNDEFINED_FOM
            else -> fom
        }

        val restrictedTom = when {
            tom isAfterLastMonthOf year -> year.lastOf()
            tom isBeforeFirstMonthOf year -> UNDEFINED_TOM
            else -> tom
        }

        return Periode(restrictedFom, restrictedTom)
    }


    companion object {
        val UNDEFINED_FOM = YearMonth.of(9999, Month.DECEMBER)
        val UNDEFINED_TOM = YearMonth.of(0, Month.JANUARY)
    }
}

enum class TimeUnit { MONTHS }

private infix fun YearMonth.isBeforeFirstMonthOf(year: Int) = this.isBefore(year.firstOf())
private infix fun YearMonth.isAfterLastMonthOf(year: Int) = this.isAfter(year.lastOf())

private fun Int.firstOf() = YearMonth.of(this, Month.JANUARY)
private fun Int.lastOf() = YearMonth.of(this, Month.DECEMBER)

internal infix fun LocalDate.periode(to: LocalDate) = Periode(YearMonth.from(this), YearMonth.from(to))




package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.util.yearmonth.rangeTo
import java.time.LocalDate
import java.time.YearMonth

internal class Periode private constructor(private val months: List<YearMonth>) {

    fun countMonths(): Int = months.size

    infix fun restrictTo(year: Int): Periode = Periode(months.filter { it.year == year })

    companion object {
        fun cratePeriode(fom: LocalDate, tom: LocalDate) = Periode((YearMonth.from(fom)..YearMonth.from(tom)).toList())
    }
}
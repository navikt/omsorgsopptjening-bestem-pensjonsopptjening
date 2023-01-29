package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.util.yearmonth

import java.time.YearMonth

class YearMonthIterator(start: YearMonth, private val endInclusive: YearMonth) : Iterator<YearMonth> {
    private var current = start

    override fun hasNext() = current <= endInclusive

    override fun next() = current.also { current = current.plusMonths(1) }
}
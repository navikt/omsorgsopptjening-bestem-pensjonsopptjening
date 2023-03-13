package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.yearmonth.rangeTo
import java.time.YearMonth

class UtbetalingMoneder private constructor(private val months: Set<YearMonth> = setOf()) {

    constructor(fom: YearMonth, tom: YearMonth) : this((fom..tom).toSet())
    constructor() : this(setOf())

    fun antall(): Int = months.size

    infix fun begrensTilAr(ar: Int): UtbetalingMoneder = UtbetalingMoneder(months.filter { it.year == ar }.toSet())

    operator fun plus(other: UtbetalingMoneder) = UtbetalingMoneder(this.months + other.months)
}

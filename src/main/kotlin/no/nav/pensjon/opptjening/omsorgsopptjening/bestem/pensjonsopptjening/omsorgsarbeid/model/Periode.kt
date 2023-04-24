package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.yearmonth.rangeTo
import java.time.YearMonth

class Periode private constructor(private val months: Set<YearMonth> = setOf()) {

    constructor(fom: YearMonth, tom: YearMonth) : this((fom..tom).toSet())

    constructor() : this(setOf())

    fun antallMoneder(): Int = months.size

    fun overlapper(vararg ar: Int): Boolean = months.map{it.year}.any { ar.contains(it) }

    infix fun begrensTilAr(ar: Int): Periode = Periode(months.filter { it.year == ar }.toSet())

    operator fun plus(other: Periode) = Periode(this.months + other.months)

    operator fun minus(other: Periode) = Periode(this.months - other.months)
}

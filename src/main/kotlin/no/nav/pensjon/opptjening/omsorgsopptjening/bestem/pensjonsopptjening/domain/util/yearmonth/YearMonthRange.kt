package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.util.yearmonth

import java.time.YearMonth

operator fun YearMonth.rangeTo(other: YearMonth) = YearMonthProgression(this, other)
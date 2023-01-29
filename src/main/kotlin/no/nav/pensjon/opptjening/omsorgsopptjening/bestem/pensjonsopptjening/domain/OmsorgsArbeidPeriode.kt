package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.Months.Companion.crateMonths
import java.time.LocalDate

class OmsorgsArbeidPeriode(
    private val fom: LocalDate,
    private val tom: LocalDate
) {
    val monthsWithPayment get() = crateMonths(fom,  tom)
}
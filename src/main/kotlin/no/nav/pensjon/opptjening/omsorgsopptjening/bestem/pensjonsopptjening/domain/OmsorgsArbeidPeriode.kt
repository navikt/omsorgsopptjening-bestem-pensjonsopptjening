package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.Periode.Companion.cratePeriode
import java.time.LocalDate

class OmsorgsArbeidPeriode(
    private val fom: LocalDate,
    private val tom: LocalDate
) {
    fun monthsInYear(year: Int): Int = (cratePeriode(fom,  tom) restrictTo year).countMonths()

}
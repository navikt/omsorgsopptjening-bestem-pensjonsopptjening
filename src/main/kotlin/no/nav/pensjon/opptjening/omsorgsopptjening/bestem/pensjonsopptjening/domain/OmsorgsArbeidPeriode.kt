package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.TimeUnit.MONTHS
import java.time.LocalDate

class OmsorgsArbeidPeriode(private val fom: LocalDate, private val tom: LocalDate) {

    fun monthsInYear(year: Int): Long = ((fom periode tom) restrictTo year) count MONTHS

}
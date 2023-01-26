package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class OmsorgsArbeidPeriode(private val fom: LocalDate, private val tom: LocalDate) {

    fun months() = ChronoUnit.MONTHS.between(fom, tom).plus(1)

}
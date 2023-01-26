package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import java.time.temporal.ChronoUnit

class OmsorgsArbeid internal constructor(private val omsorgsYter: Omsorgsyter) {
    fun monthsOfOmsorg() = ChronoUnit.MONTHS.between(omsorgsYter.fom,omsorgsYter.tom).plus(1)

}

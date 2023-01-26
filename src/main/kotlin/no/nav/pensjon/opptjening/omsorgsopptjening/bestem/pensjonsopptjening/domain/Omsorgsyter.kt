package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class Omsorgsyter(private val omsorgsArbeidPeriode: List<OmsorgsArbeidPeriode>) {
    fun monthsOfOmsorg(omsorgsAr: Int): Long = omsorgsArbeidPeriode.firstOrNull()?.monthsInYear(omsorgsAr) ?: 0
}
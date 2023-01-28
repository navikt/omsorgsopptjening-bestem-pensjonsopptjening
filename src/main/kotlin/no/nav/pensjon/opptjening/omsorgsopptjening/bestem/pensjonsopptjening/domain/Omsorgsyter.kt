package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class Omsorgsyter(private val omsorgsArbeidPeriode: List<OmsorgsArbeidPeriode>) {
    fun monthsOfOmsorg(omsorgsAr: Int): Int = omsorgsArbeidPeriode.firstOrNull()?.monthsInYear(omsorgsAr) ?: 0
}
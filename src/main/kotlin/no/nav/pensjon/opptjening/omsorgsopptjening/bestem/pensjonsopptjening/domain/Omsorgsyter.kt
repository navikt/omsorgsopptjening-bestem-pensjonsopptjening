package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class Omsorgsyter(private val omsorgsArbeidPeriode: List<OmsorgsArbeidPeriode>) {
    fun monthsOfOmsorg(): Long = omsorgsArbeidPeriode.firstOrNull()?.months() ?: 0
}
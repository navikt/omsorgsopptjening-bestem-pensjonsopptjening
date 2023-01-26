package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class OmsorgsArbeid internal constructor(val omsorgsYter: List<OmsorgsYter> = listOf()) {
    fun monthsOfOmsorg(): Int = 6
}

class OmsorgsArbeidFactory {
    companion object {
        fun createOmsorgsArbeid(): OmsorgsArbeid {
            return OmsorgsArbeid()
        }
    }
}

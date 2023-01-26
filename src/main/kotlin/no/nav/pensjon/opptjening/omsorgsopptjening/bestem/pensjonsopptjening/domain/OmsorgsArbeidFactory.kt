package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

class OmsorgsArbeidFactory {
    companion object {
        fun createOmsorgsArbeid(input: no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeid): OmsorgsArbeid {
            val output: OmsorgsArbeid = OmsorgsArbeid()
            output.omsorgsYter = emptyList()
            return OmsorgsArbeid()
        }
    }
}
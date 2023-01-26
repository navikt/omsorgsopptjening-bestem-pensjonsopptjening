package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel

class OmsorgsArbeidFactory {
    companion object {
        fun createOmsorgsArbeid(inputOmsorgsArbeid: OmsorgsArbeidModel): OmsorgsArbeid {
            return OmsorgsArbeid(Omsorgsyter(inputOmsorgsArbeid.omsorgsyter.utbetalingsperioder[0].fom,
                inputOmsorgsArbeid.omsorgsyter.utbetalingsperioder[0].tom))
        }
    }
}
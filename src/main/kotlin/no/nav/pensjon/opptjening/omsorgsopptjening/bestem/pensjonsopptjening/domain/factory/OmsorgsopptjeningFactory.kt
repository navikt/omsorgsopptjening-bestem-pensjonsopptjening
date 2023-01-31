package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.OmsorgsOpptjening

class OmsorgsopptjeningFactory {
    companion object {
        fun createOmsorgsopptjening(omsorgsArbeidModel: OmsorgsArbeidModel): OmsorgsOpptjening {
            return OmsorgsOpptjening(
                omsorgsAr = omsorgsArbeidModel.omsorgsAr.toInt(),
                involvertePerson = PersonFactory.createPerson(omsorgsArbeidModel.omsorgsyter.fnr),
                omsorgsarbeidSak = OmsorgsArbeidFactory.createOmsorgsArbeid(omsorgsArbeidModel)
            )
        }
    }
}
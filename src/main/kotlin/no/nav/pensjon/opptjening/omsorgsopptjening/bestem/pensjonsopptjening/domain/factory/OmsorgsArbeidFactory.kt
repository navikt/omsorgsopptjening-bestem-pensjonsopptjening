package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidsUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.Omsorgsyter

class OmsorgsArbeidFactory {
    companion object {
        fun createOmsorgsArbeid(inputOmsorgsArbeid: OmsorgsArbeidModel): OmsorgsArbeidSak {
            return OmsorgsArbeidSak(
                omsorgsYter = Omsorgsyter(
                    person = PersonFactory.createPerson(inputOmsorgsArbeid.omsorgsyter.fnr),
                    omsorgsArbeidsUtbetalinger = inputOmsorgsArbeid.omsorgsyter.utbetalingsperioder.map {
                        OmsorgsArbeidsUtbetalinger(it.fom, it.tom)
                    }
                )
            )
        }
    }
}
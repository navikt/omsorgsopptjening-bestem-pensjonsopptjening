package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidsUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.Omsorgsarbeid

class OmsorgsArbeidSakFactory {
    companion object {
        fun createOmsorgsArbeidSak(inputOmsorgsArbeid: OmsorgsArbeidModel): OmsorgsArbeidSak {
            return OmsorgsArbeidSak(
                utfortOmsorgsArbeid = listOf(
                    Omsorgsarbeid(
                        person = PersonFactory.createPerson(inputOmsorgsArbeid.omsorgsyter.fnr),
                        omsorgsArbeidsUtbetalinger = inputOmsorgsArbeid.omsorgsyter.utbetalingsperioder.map {
                            OmsorgsArbeidsUtbetalinger(it.fom, it.tom)
                        }
                    )
                )
            )
        }
    }
}
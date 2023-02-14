package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidsUtbetalinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.Omsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid

class OmsorgsArbeidSakFactory {
    companion object {
        fun createOmsorgsArbeidSak(inputOmsorgsArbeid: OmsorgsArbeid): OmsorgsArbeidSak {
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
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsArbeidModel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.OmsorgsArbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.Omsorgsyter

class OmsorgsArbeidFactory {
    companion object {
        fun createOmsorgsArbeid(inputOmsorgsArbeid: OmsorgsArbeidModel): OmsorgsArbeid {
            return OmsorgsArbeid(
                omsorgsAr = inputOmsorgsArbeid.omsorgsAr.toInt(),
                omsorgsYter = Omsorgsyter(
                    inputOmsorgsArbeid.omsorgsyter.utbetalingsperioder.map {
                        OmsorgsArbeidPeriode(it.fom, it.tom)
                    }
                )

            )
        }
    }
}
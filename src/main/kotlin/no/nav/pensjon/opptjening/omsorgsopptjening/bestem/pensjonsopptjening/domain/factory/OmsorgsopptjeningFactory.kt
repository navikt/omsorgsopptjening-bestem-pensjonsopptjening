package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.Omsorgsyter

class OmsorgsopptjeningFactory {
    companion object {
        fun createOmsorgsopptjening(omsorgsyter: Omsorgsyter, omsorgsar: Int): OmsorgsOpptjening {
            return OmsorgsOpptjening(omsorgsyter, omsorgsar)
        }
    }
 }
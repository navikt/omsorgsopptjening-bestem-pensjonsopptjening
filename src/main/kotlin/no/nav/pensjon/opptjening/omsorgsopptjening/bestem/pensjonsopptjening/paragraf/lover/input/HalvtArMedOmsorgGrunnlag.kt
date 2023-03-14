package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid

data class HalvtArMedOmsorgGrunnlag(
    val omsorgsArbeid: List<OmsorgsArbeid>,
    val omsorgsMottaker: Person?,
    val omsorgsAr: Int
)
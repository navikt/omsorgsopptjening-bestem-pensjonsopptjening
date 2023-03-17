package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid

data class GrunnlagOmsorgForBarnUnder6(
    val omsorgsArbeid: List<OmsorgsArbeid>,
    val omsorgsmottaker: Person,
    val omsorgsAr: Int
)
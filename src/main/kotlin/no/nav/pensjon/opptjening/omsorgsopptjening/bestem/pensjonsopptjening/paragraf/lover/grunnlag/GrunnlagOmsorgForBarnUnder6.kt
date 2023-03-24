package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person

data class GrunnlagOmsorgForBarnUnder6(
    val omsorgsArbeid: List<OmsorgsarbeidPeriode>,
    val omsorgsmottaker: Person,
    val omsorgsAr: Int
)
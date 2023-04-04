package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person


data class GrunnlagDeltOmsorgForBarnUnder6(
    val andreOmsorgsGivere: List<OmsorgsGiverDeltOmsorg>,
    val omsorgsArbeid50Prosent: List<OmsorgsarbeidPeriode>,
    val omsorgsmottaker: Person,
    val omsorgsAr: Int
)
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person


data class GrunnlagDeltOmsorgForBarnUnder6(
    val omsorgsAr: Int,
    val omsorgsyter: Person,
    val omsorgsmottaker: Person,
    val omsorgsArbeid50Prosent: List<OmsorgsarbeidPeriode>,
    val andreParter: List<AnnenPart>,
) {}
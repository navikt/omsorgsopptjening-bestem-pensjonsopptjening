package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall

data class GrunnlagOmsorgForBarnUnder6(
    val omsorgsAr: Int,
    val omsorgsmottaker: Person,
    val utfallAbsolutteKrav: Utfall,
    val omsorgsArbeid100Prosent: List<OmsorgsarbeidPeriode>,
)
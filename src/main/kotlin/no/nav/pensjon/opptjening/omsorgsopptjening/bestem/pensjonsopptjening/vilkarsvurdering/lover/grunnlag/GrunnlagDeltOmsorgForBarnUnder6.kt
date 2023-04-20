package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall


data class GrunnlagDeltOmsorgForBarnUnder6(
    val omsorgsAr: Int,
    val omsorgsyter: Person,
    val omsorgsmottaker: Person,
    val utfallPersonVilkarsvurdering: Utfall,
    val omsorgsArbeid50Prosent: List<OmsorgVedtakPeriode>,
    val andreParter: List<AnnenPart>,
)
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsvedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall

data class AnnenPart(
    val omsorgsyter: Person,
    val omsorgsArbeid50Prosent: List<OmsorgsvedtakPeriode>,
    val harInvilgetOmsorgForUrelaterBarn: Boolean,
    val utfallAbsolutteKrav: Utfall,
)
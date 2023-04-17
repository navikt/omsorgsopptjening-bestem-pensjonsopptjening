package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall

data class AnnenPart(
    val omsorgsyter: Person,
    val omsorgsArbeid50Prosent: List<OmsorgsarbeidPeriode>,
    val harInvilgetOmsorgForUrelaterBarn: Boolean,
    val utfallAbsolutteKrav: Utfall,
)
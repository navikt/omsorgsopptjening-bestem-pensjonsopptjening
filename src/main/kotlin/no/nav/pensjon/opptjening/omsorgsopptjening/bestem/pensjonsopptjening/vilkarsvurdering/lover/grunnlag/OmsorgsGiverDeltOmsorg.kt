package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidPeriode

data class OmsorgsGiverDeltOmsorg(
    val omsorgsArbeid50Prosent: List<OmsorgsarbeidPeriode>,
    val harInvilgetOmsorgForUrelaterBarn: Boolean
)
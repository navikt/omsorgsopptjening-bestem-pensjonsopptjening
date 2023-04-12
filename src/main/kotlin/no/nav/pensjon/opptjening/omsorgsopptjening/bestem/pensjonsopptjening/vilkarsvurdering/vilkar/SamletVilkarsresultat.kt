package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot

data class SamletVilkarsresultat(
    val snapshot: OmsorgsarbeidSnapshot,
    var individueltVilkarsresultat: VilkarsVurdering<*>? = null,
    var sammenstiltVilkarsresultat: VilkarsVurdering<*>? = null,
)
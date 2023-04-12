package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot

data class Vilkarsresultat(
    val snapshot: OmsorgsarbeidSnapshot,
    var individueltVilkarsVurdering: VilkarsVurdering<*>? = null,
    var sammenstiltVilkarsVurdering: VilkarsVurdering<*>? = null,
)
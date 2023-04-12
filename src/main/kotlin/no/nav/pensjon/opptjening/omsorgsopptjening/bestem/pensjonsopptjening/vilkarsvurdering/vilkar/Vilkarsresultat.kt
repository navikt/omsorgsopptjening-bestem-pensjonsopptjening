package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.eller

data class Vilkarsresultat(
    val snapshot: OmsorgsarbeidSnapshot,
    var individueltVilkarsVurdering: VilkarsVurdering<*>? = null,
    var sammenstiltVilkarsVurdering: VilkarsVurdering<*>? = null,
){

    fun getOmsorgsyter() = snapshot.omsorgsyter

    fun getOmsorgsAr() = snapshot.omsorgsAr

    fun getUtfall() = eller(individueltVilkarsVurdering!!, sammenstiltVilkarsVurdering!!).utfall

    fun hentVilkarsVurderingerFullOmsorgForBarnUnder6() = individueltVilkarsVurdering!!.hentVilkarsVurderingerFullOmsorgForBarnUnder6()
}
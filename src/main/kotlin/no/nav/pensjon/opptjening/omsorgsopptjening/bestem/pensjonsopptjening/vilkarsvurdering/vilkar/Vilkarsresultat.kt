package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.operator.Og.Companion.og

data class Vilkarsresultat(
    val snapshot: OmsorgsGrunnlag,
    var personVilkarsvurdering: VilkarsVurdering<*>? = null,
    var individueltVilkarsVurdering: VilkarsVurdering<*>? = null,
    var sammenstiltVilkarsVurdering: VilkarsVurdering<*>? = null,
) {

    fun getOmsorgsyter() = snapshot.omsorgsyter

    fun getOmsorgsAr() = snapshot.omsorgsAr

    fun getUtfall() = og(
        personVilkarsvurdering!!,
        eller(individueltVilkarsVurdering!!, sammenstiltVilkarsVurdering!!)
    ).utfall

    fun hentVilkarsVurderingerFullOmsorgForBarnUnder6() =
        individueltVilkarsVurdering!!.hentVilkarsVurderingerFullOmsorgForBarnUnder6()
}
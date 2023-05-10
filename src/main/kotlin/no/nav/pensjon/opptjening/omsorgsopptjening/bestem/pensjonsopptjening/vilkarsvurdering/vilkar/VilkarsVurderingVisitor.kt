package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.FullOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsYterOver16Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.OmsorgsyterUnder70Ar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.lover.grunnlag.GrunnlagOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.AlleVilkarsVurderinger.Companion.hentAlleVilkarsVurderinger

interface VilkarsVurderingVisitor {
    fun visit(vilkarsVurdering: VilkarsVurdering<*>)
}

class AlleVilkarsVurderinger private constructor() : VilkarsVurderingVisitor {
    private val alleVilkarsVurderinger: MutableList<VilkarsVurdering<*>> = mutableListOf()

    override fun visit(vilkarsVurdering: VilkarsVurdering<*>) {
        alleVilkarsVurderinger.add(vilkarsVurdering)
    }

    companion object {
        fun hentAlleVilkarsVurderinger(vilkarsVurdering: VilkarsVurdering<*>): List<VilkarsVurdering<*>> {
            val visitor = AlleVilkarsVurderinger()
            vilkarsVurdering.accept(visitor)
            return visitor.alleVilkarsVurderinger.toList()
        }
    }
}

fun VilkarsVurdering<*>.hentVilkarsVurderingerFullOmsorgForBarnUnder6() : List<VilkarsVurdering<GrunnlagOmsorgForBarnUnder6>>{
    return hentAlleVilkarsVurderinger(this)
        .filter { it.vilkar is FullOmsorgForBarnUnder6 }
        .map { it as VilkarsVurdering<GrunnlagOmsorgForBarnUnder6> }
}

fun VilkarsVurdering<*>.hentVilkarsVurderingerFullOmsorgForBarnUnder6(vararg forUtfall: Utfall) : List<VilkarsVurdering<GrunnlagOmsorgForBarnUnder6>>{
    return hentVilkarsVurderingerFullOmsorgForBarnUnder6()
        .filter { forUtfall.contains(it.utfall) }
}


fun VilkarsVurdering<*>.hentOmsorgsyterOver16() = hentAlleVilkarsVurderinger(this).first { it.vilkar is OmsorgsYterOver16Ar }

fun VilkarsVurdering<*>.hentOmsorgsyterUnder70() = hentAlleVilkarsVurderinger(this).first { it.vilkar is OmsorgsyterUnder70Ar }
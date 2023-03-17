package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.AlleVilkarsVurderinger.Companion.hentAlleVilkarsVurderinger

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

fun hentHalvtArMedOmsorgVilkarsVurderinger(vilkarsVurdering: VilkarsVurdering<*>) : List<VilkarsVurdering<HalvtArMedOmsorgGrunnlag>>{
    return hentAlleVilkarsVurderinger(vilkarsVurdering)
        .filter { it.vilkar is HalvtArMedOmsorgForBarnUnder6 }
        .map { it as VilkarsVurdering<HalvtArMedOmsorgGrunnlag> }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag

interface VilkarsVurderingVisitor {
    fun visit(vilkarsVurdering: VilkarsVurdering<*>)
}

class HalvtArMedOmsorgVisitor private constructor() : VilkarsVurderingVisitor {

    private val halvtArMedOmsorgList: MutableList<VilkarsVurdering<HalvtArMedOmsorgGrunnlag>> = mutableListOf()

    override fun visit(vilkarsVurdering: VilkarsVurdering<*>) {
        if (vilkarsVurdering.vilkar is HalvtArMedOmsorgForBarnUnder6) {
            halvtArMedOmsorgList.add(vilkarsVurdering as VilkarsVurdering<HalvtArMedOmsorgGrunnlag>)
        }
    }

    companion object {
        fun hentHalvtArMedOmsorg(vilkarsResultat: VilkarsResultat<*>): List<VilkarsVurdering<HalvtArMedOmsorgGrunnlag>> {
            val visitor = HalvtArMedOmsorgVisitor()
            vilkarsResultat.vilkarsVurdering.accept(visitor)
            return visitor.halvtArMedOmsorgList.toList()
        }
    }
}
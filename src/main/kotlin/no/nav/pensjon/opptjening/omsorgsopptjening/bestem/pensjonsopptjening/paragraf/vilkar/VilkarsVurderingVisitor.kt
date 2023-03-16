package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag

interface VilkarsVurderingVisitor {
    fun visit(vilkarsVurdering: VilkarsVurdering<*>)
}

class HalvtArMedOmsorgVisitor private constructor() : VilkarsVurderingVisitor {

    private val halvtArMedOmsorgList: MutableList<VilkarsResultat<HalvtArMedOmsorgGrunnlag>> = mutableListOf()

    override fun visit(vilkarsVurdering: VilkarsVurdering<*>) {
        if(vilkarsVurdering.grunnlag is HalvtArMedOmsorgForBarnUnder6 && vilkarsVurdering.vilkar is HalvtArMedOmsorgForBarnUnder6){
            halvtArMedOmsorgList.add(vilkarsVurdering as VilkarsResultat<HalvtArMedOmsorgGrunnlag>)
        }    }

    companion object{
        fun hentHalvtArMedOmsorg(vilkarsResultat: VilkarsResultat<*>) : List<VilkarsResultat<HalvtArMedOmsorgGrunnlag>>{
            val visitor = HalvtArMedOmsorgVisitor()
            vilkarsResultat.vilkarsVurdering.accept(visitor)
            return visitor.halvtArMedOmsorgList.toList()
        }
    }
}
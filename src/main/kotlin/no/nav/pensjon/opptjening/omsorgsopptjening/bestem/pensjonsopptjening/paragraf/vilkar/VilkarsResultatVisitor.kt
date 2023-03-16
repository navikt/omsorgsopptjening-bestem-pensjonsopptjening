package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.HalvtArMedOmsorgForBarnUnder6
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.lover.input.HalvtArMedOmsorgGrunnlag

interface VilkarsResultatVisitor {
    fun visit(vilkarsResultat: VilkarsResultat<*>)
}

class HalvtArMedOmsorgVisitor private constructor() : VilkarsResultatVisitor {

    private val halvtArMedOmsorgList: MutableList<VilkarsResultat<HalvtArMedOmsorgGrunnlag>> = mutableListOf()

    override fun visit(vilkarsResultat: VilkarsResultat<*>) {
        if(vilkarsResultat.grunnlag is HalvtArMedOmsorgForBarnUnder6 && vilkarsResultat.vilkar is HalvtArMedOmsorgForBarnUnder6){
            halvtArMedOmsorgList.add(vilkarsResultat as VilkarsResultat<HalvtArMedOmsorgGrunnlag>)
        }
    }

    companion object{
        fun hentHalvtArMedOmsorg(vilkarsResultat: VilkarsResultat<*>) : List<VilkarsResultat<HalvtArMedOmsorgGrunnlag>>{
            val visitor = HalvtArMedOmsorgVisitor()
            vilkarsResultat.accept(visitor)
            return visitor.halvtArMedOmsorgList.toList()
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

interface VilkarsVurderingVisitor {
    fun visit(vilkarsVurdering: VilkarsVurdering<*>)
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

open class VilkarsVurdering<Grunnlag : Any>(
    val vilkar: Vilkar<Grunnlag>,
    val grunnlag: Grunnlag
) {
    fun accept(vilkarsVurderingVisitor: VilkarsVurderingVisitor) {
        vilkarsVurderingVisitor.visit(this)
        if (grunnlag is List<*>) grunnlag.forEach { if (it is VilkarsVurdering<*>) it.accept(vilkarsVurderingVisitor) }
    }

    open fun utfor(): VilkarsResultat<Grunnlag> {
        return VilkarsResultat(
            avgjorelse = vilkar.avgjorelsesFunksjon(grunnlag),
            grunnlag = grunnlag,
            vilkarsVurdering = this
        )
    }
}
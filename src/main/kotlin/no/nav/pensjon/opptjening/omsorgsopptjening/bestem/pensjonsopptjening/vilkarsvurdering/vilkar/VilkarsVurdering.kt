package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar

open class VilkarsVurdering<Grunnlag : Any>(
    val vilkar: Vilkar<Grunnlag>,
    val grunnlag: Grunnlag
) {

    val utfall = vilkar.utfallsFunksjon(grunnlag)

    fun accept(vilkarsVurderingVisitor: VilkarsVurderingVisitor) {
        vilkarsVurderingVisitor.visit(this)
        if (grunnlag is List<*>) grunnlag.forEach { if (it is VilkarsVurdering<*>) it.accept(vilkarsVurderingVisitor) }
    }
}

enum class Utfall {
    INVILGET,
    AVSLAG,
    SAKSBEHANDLING,
    MANGLER_ANNEN_OMSORGSYTER
}
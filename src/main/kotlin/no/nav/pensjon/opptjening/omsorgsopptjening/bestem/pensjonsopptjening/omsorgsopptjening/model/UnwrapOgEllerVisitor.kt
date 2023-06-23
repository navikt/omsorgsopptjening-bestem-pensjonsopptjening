package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

/**
 * Unwraps all vilkårsvurderinger wrapped in [OgVurdering] or [EllerVurdering] to create a "flat" representation
 * of all vilkårsvurderinger contained, excluding [Og] and [Eller].
 * 
 * og(a,b,eller(c,d)) yields [a,b,c,d].
 */
class UnwrapOgEllerVisitor private constructor() : VilkarsVurderingVisitor {
    private val vilkårsvurderinger: MutableList<VilkarsVurdering<*>> = mutableListOf()

    override fun visit(vilkarsVurdering: VilkarsVurdering<*>) {
        when(vilkarsVurdering){
            is EllerVurdering -> {
                vilkarsVurdering.grunnlag.map { it.accept(this) }
            }
            is OgVurdering -> {
                vilkarsVurdering.grunnlag.map { it.accept(this) }
            }
            else -> {
                vilkårsvurderinger.add(vilkarsVurdering)
            }
        }
    }

    companion object {
        fun unwrap(vilkarsVurdering: VilkarsVurdering<*>): List<VilkarsVurdering<*>> {
            return UnwrapOgEllerVisitor().let {
                vilkarsVurdering.accept(it)
                it.vilkårsvurderinger
            }
        }
    }
}
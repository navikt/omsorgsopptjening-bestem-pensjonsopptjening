package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkarsVurdering<Grunnlag : Any> {
    abstract val grunnlag: Grunnlag
    abstract val utfall: VilkårsvurderingUtfall

    fun accept(vilkarsVurderingVisitor: VilkarsVurderingVisitor) {
        vilkarsVurderingVisitor.visit(this)
    }
}

sealed class ParagrafVurdering<T : ParagrafGrunnlag> : VilkarsVurdering<T>()

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erAvslått(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { !erInnvilget<T>() }.single()
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erInnvilget(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { it.utfall.erInnvilget() }.single()
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erEnesteAvslag(): Boolean {
    return erAvslått<T>() && UnwrapOgEllerVisitor.unwrap(this).count { !it.utfall.erInnvilget() } == 1
}

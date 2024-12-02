package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkarsVurdering<Grunnlag : Any> {
    abstract val grunnlag: Grunnlag
    abstract val utfall: VilkårsvurderingUtfall

    fun accept(vilkarsVurderingVisitor: VilkarsVurderingVisitor) {
        vilkarsVurderingVisitor.visit(this)
    }

    open fun hentOppgaveopplysninger(behandling: FullførtBehandling): Oppgaveopplysninger = Oppgaveopplysninger.Ingen
}

sealed class ParagrafVurdering<T : ParagrafGrunnlag> : VilkarsVurdering<T>()

@JvmName("reifiedErAvslått")
inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erAvslått(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { it.erAvslått() }.single()
}

fun VilkarsVurdering<*>.erAvslått(): Boolean {
    return utfall.erAvslag()
}

@JvmName("reifiedErInnvilget")
inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erInnvilget(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { it.erInnvilget() }.single()
}

fun VilkarsVurdering<*>.erInnvilget(): Boolean {
    return utfall.erInnvilget()
}

@JvmName("reifiedErUbestemt")
inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erUbestemt(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { erUbestemt() }.single()
}

fun VilkarsVurdering<*>.erUbestemt(): Boolean {
    return utfall.erUbestemt()
}

fun VilkarsVurdering<*>.finnAlleInnvilget(): List<VilkarsVurdering<*>> {
    return UnwrapOgEllerVisitor.unwrap(this).filter { it.erInnvilget() }
}

fun VilkarsVurdering<*>.finnAlleAvslatte(): List<VilkarsVurdering<*>> {
    return UnwrapOgEllerVisitor.unwrap(this).filter { it.erAvslått() }
}

fun VilkarsVurdering<*>.finnAlleUbestemte(): List<VilkarsVurdering<*>> {
    return UnwrapOgEllerVisitor.unwrap(this).filter { it.erUbestemt() }
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erEnesteAvslag(): Boolean {
    return erAvslått<T>() && UnwrapOgEllerVisitor.unwrap(this).count { it.erAvslått() } == 1
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erEnesteUbestemt(): Boolean {
    return erUbestemt<T>() && UnwrapOgEllerVisitor.unwrap(this).count { it.erUbestemt() } == 1
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.finnVurdering(): T {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().single()
}

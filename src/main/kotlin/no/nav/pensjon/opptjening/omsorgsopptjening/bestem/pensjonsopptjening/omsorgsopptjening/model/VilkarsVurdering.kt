package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

sealed class VilkarsVurdering<Grunnlag : Any> {
    abstract val grunnlag: Grunnlag
    abstract val utfall: VilkårsvurderingUtfall

    fun accept(vilkarsVurderingVisitor: VilkarsVurderingVisitor) {
        vilkarsVurderingVisitor.visit(this)
    }

    open fun hentOppgaveopplysninger(): Oppgaveopplysninger = Oppgaveopplysninger.Ingen
}

sealed class ParagrafVurdering<T : ParagrafGrunnlag> : VilkarsVurdering<T>()

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erAvslått(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { !erInnvilget<T>() }.single()
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erInnvilget(): Boolean {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().map { it.utfall.erInnvilget() }.single()
}

fun VilkarsVurdering<*>.finnAlleAvslatte(): List<VilkarsVurdering<*>> {
    return UnwrapOgEllerVisitor.unwrap(this).filter { !it.utfall.erInnvilget() }
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erEnesteAvslag(): Boolean {
    return erAvslått<T>() && UnwrapOgEllerVisitor.unwrap(this).count { !it.utfall.erInnvilget() } == 1
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.finnVurdering(): T {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().single()
}

fun VilkarsVurdering<*>.behovForManuellBehandling(): Boolean {
    return finnAlleUbestemteVilkår().isNotEmpty()
}

fun VilkarsVurdering<*>.finnAlleUbestemteVilkår(): List<VilkarsVurdering<*>> {
    return UnwrapOgEllerVisitor.unwrap(this).filter { it.utfall is VilkårsvurderingUtfall.Ubestemt }
}
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

inline fun VilkarsVurdering<*>.finnAlleAvslatte(): List<VilkarsVurdering<*>> {
    return UnwrapOgEllerVisitor.unwrap(this).filter { !it.utfall.erInnvilget() }
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.erEnesteAvslag(): Boolean {
    return erAvslått<T>() && UnwrapOgEllerVisitor.unwrap(this).count { !it.utfall.erInnvilget() } == 1
}

inline fun <reified T : ParagrafVurdering<*>> VilkarsVurdering<*>.finnVurdering(): T {
    return UnwrapOgEllerVisitor.unwrap(this).filterIsInstance<T>().single()
}

fun VilkarsVurdering<*>.behovForManuellBehandling(): Boolean {
    return hentVilkårSomMåbehandlesManuelt() != null
}

fun VilkarsVurdering<*>.hentVilkårSomMåbehandlesManuelt(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering? {
    return avslagSkyldesFlereOmsorgsytereMedLikeMangeOmsorgsmåneder()
}

/**
 * Det er bare aktuelt å lage oppgave i tilfeller hvor det ikke kan godskrives oppgjening som følge av at flere
 * omsorgsyter har like mange omsorgsmåneder for det samme barnet i løpet av omsorgsåret.
 */
private fun VilkarsVurdering<*>.avslagSkyldesFlereOmsorgsytereMedLikeMangeOmsorgsmåneder(): OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering? {
    return finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
        if (erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>() && it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder()) {
            it
        } else {
            null
        }
    }
}

sealed class Oppgaveopplysning {
    data class ToOmsorgsytereMedLikeMangeMånederOmsorg(
        val oppgaveMottaker: String,
        val annenOmsorgsyter: String,
        val omsorgsmottaker: String,
        val omsorgsår: Int,
    ) : Oppgaveopplysning()

    data object Ingen : Oppgaveopplysning()
}
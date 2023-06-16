package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


abstract class Vilkar<Grunnlag : Any> {
    abstract fun vilkarsVurder(grunnlag: Grunnlag): VilkarsVurdering<Grunnlag>
    protected abstract fun <T : Vilkar<Grunnlag>> T.bestemUtfall(grunnlag: Grunnlag): VilkårsvurderingUtfall
}

abstract class ParagrafVilkår<T : ParagrafGrunnlag>(
    val paragrafer: Set<Paragraf>
) : Vilkar<T>()

sealed class ParagrafGrunnlag
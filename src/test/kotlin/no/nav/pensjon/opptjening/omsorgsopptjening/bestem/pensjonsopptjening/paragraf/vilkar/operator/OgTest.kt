package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OgTest {

    @Test
    fun `Given all vilkar is INVILGET Then INVILGET`() {
        val ogVilkarsVurdering = og(
            returnUtfall.vilkarsVurder(grunnlag = Utfall.INVILGET),
            returnUtfall.vilkarsVurder(grunnlag = Utfall.INVILGET)
        )

        kotlin.test.assertEquals(Utfall.INVILGET, ogVilkarsVurdering.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "SAKSBEHANDLING, SAKSBEHANDLING",
        "SAKSBEHANDLING, INVILGET",
        "INVILGET, SAKSBEHANDLING",
        "SAKSBEHANDLING, AVSLAG",
        "AVSLAG, SAKSBEHANDLING",
    )
    fun `Given at least one vilkar is SAKSBEHANDLING and no vilkar MANGLER_ANNEN_OMSORGSYTER Then SAKSBEHANDLING`(
        utfall1: Utfall,
        utfall2: Utfall,
    ) {
        val ogVilkarsVurdering = og(
            returnUtfall.vilkarsVurder(grunnlag = utfall1),
            returnUtfall.vilkarsVurder(grunnlag = utfall2)
        )

        Assertions.assertEquals(Utfall.SAKSBEHANDLING, ogVilkarsVurdering.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "MANGLER_ANNEN_OMSORGSYTER, MANGLER_ANNEN_OMSORGSYTER",
        "SAKSBEHANDLING, MANGLER_ANNEN_OMSORGSYTER",
        "MANGLER_ANNEN_OMSORGSYTER, SAKSBEHANDLING",
        "MANGLER_ANNEN_OMSORGSYTER, AVSLAG",
        "AVSLAG, MANGLER_ANNEN_OMSORGSYTER",
    )
    fun `Given one vilkar MANGLER_ANNEN_OMSORGSYTER Then MANGLER_ANNEN_OMSORGSYTER`(
        utfall1: Utfall,
        utfall2: Utfall,
    ) {
        val ogVilkarsVurdering = og(
            returnUtfall.vilkarsVurder(grunnlag = utfall1),
            returnUtfall.vilkarsVurder(grunnlag = utfall2)
        )

        Assertions.assertEquals(Utfall.MANGLER_ANNEN_OMSORGSYTER, ogVilkarsVurdering.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "AVSLAG, INVILGET",
        "INVILGET, AVSLAG",
        "AVSLAG, AVSLAG",
    )
    fun `Given at least one vilkar with AVSLAG and no vilkar with SAKSBEHANDLING or MANGLER_ANNEN_OMSORGSYTER Then AVSLAG`(
        utfall1: Utfall,
        utfall2: Utfall,
    ) {
        val ogVilkarsVurdering = og(
            returnUtfall.vilkarsVurder(grunnlag = utfall1),
            returnUtfall.vilkarsVurder(grunnlag = utfall2)
        )

        Assertions.assertEquals(Utfall.AVSLAG, ogVilkarsVurdering.utfall)
    }



    companion object {
        private val returnUtfall = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            utfallsFunksjon = fun(utfall: Utfall) = utfall
        )
    }
}
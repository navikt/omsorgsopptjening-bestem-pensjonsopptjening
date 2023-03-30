package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Eller.Companion.eller
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


internal class EllerTest {

    @ParameterizedTest
    @CsvSource(
        "INVILGET, INVILGET",
        "AVSLAG, INVILGET",
        "INVILGET, AVSLAG",
        "SAKSBEHANDLING, INVILGET",
        "INVILGET, SAKSBEHANDLING",
    )
    fun `Given one vilkar INVILGET Then INVILGET`(
        utfall1: Utfall,
        utfall2: Utfall,
    ) {
        val vilkarsVurdering = eller(
            returnUtfall.vilkarsVurder(grunnlag = utfall1),
            returnUtfall.vilkarsVurder(grunnlag = utfall2)
        )

        assertEquals(Utfall.INVILGET, vilkarsVurdering.utfall)
    }

    @ParameterizedTest
    @CsvSource(
        "AVSLAG, AVSLAG, SAKSBEHANDLING",
        "AVSLAG, SAKSBEHANDLING, AVSLAG",
        "SAKSBEHANDLING, AVSLAG, AVSLAG",
        "SAKSBEHANDLING, SAKSBEHANDLING, AVSLAG",
        "SAKSBEHANDLING, AVSLAG, SAKSBEHANDLING",
        "AVSLAG, SAKSBEHANDLING, SAKSBEHANDLING",
        "SAKSBEHANDLING, SAKSBEHANDLING, SAKSBEHANDLING",
    )
    fun `Given at least one vilkar SAKSBEHANDLING and no vilkar INVILGET or MANGLER_ANNEN_OMSORGSYTER Then SAKSBEHANDLING`(
        utfall1: Utfall,
        utfall2: Utfall,
        utfall3: Utfall,
    ) {
        val vilkarsVurdering = eller(
            returnUtfall.vilkarsVurder(grunnlag = utfall1),
            returnUtfall.vilkarsVurder(grunnlag = utfall2),
            returnUtfall.vilkarsVurder(grunnlag = utfall3)
        )

        assertEquals(Utfall.SAKSBEHANDLING, vilkarsVurdering.utfall)
    }


    @ParameterizedTest
    @CsvSource(
        "AVSLAG, SAKSBEHANDLING, MANGLER_ANNEN_OMSORGSYTER",
        "SAKSBEHANDLING, MANGLER_ANNEN_OMSORGSYTER, AVSLAG",
        "MANGLER_ANNEN_OMSORGSYTER, AVSLAG, SAKSBEHANDLING",
        "MANGLER_ANNEN_OMSORGSYTER, MANGLER_ANNEN_OMSORGSYTER, SAKSBEHANDLING",
        "MANGLER_ANNEN_OMSORGSYTER, SAKSBEHANDLING, MANGLER_ANNEN_OMSORGSYTER",
        "SAKSBEHANDLING, MANGLER_ANNEN_OMSORGSYTER, MANGLER_ANNEN_OMSORGSYTER",
        "MANGLER_ANNEN_OMSORGSYTER, MANGLER_ANNEN_OMSORGSYTER, MANGLER_ANNEN_OMSORGSYTER",
    )
    fun `Given at least one vilkar MANGLER_ANNEN_OMSORGSYTER and no vilkar INVILGET Then MANGLER_ANNEN_OMSORGSYTER`(
        utfall1: Utfall,
        utfall2: Utfall,
        utfall3: Utfall,
    ) {
        val vilkarsVurdering = eller(
            returnUtfall.vilkarsVurder(grunnlag = utfall1),
            returnUtfall.vilkarsVurder(grunnlag = utfall2),
            returnUtfall.vilkarsVurder(grunnlag = utfall3)
        )

        assertEquals(Utfall.MANGLER_ANNEN_OMSORGSYTER, vilkarsVurdering.utfall)
    }

    @Test
    fun `Given All vilkar AVSLAG Then AVSLAG`() {
        val vilkarsVurdering = eller(
            returnUtfall.vilkarsVurder(grunnlag = Utfall.AVSLAG),
            returnUtfall.vilkarsVurder(grunnlag = Utfall.AVSLAG)
        )

        assertEquals(Utfall.AVSLAG, vilkarsVurdering.utfall)
    }

    companion object {
        private val returnUtfall = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            utfallsFunksjon = fun(utfall: Utfall) = utfall
        )
    }
}
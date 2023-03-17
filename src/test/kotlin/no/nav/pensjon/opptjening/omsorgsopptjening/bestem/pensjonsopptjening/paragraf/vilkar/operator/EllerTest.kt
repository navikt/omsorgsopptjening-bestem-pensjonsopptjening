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
    fun `Given one vilkar INVILGET When evaluating eller Then INVILGET`(
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
        "AVSLAG, AVSLAG, SAKSBEHANDLING, SAKSBEHANDLING",
        "AVSLAG, SAKSBEHANDLING, AVSLAG, SAKSBEHANDLING",
        "SAKSBEHANDLING, AVSLAG, AVSLAG, SAKSBEHANDLING",
        "SAKSBEHANDLING, SAKSBEHANDLING, AVSLAG, SAKSBEHANDLING",
        "SAKSBEHANDLING, AVSLAG, SAKSBEHANDLING, SAKSBEHANDLING",
        "AVSLAG, SAKSBEHANDLING, SAKSBEHANDLING, SAKSBEHANDLING",
        "SAKSBEHANDLING, SAKSBEHANDLING, SAKSBEHANDLING, SAKSBEHANDLING",
    )
    fun `Given at least one vilkar SAKSBEHANDLING and no vilkar INVILGET When evaluating eller Then SAKSBEHANDLING`(
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

    @Test
    fun `Given All vilkar AVSLAG When evaluating eller Then AVSLAG`() {
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
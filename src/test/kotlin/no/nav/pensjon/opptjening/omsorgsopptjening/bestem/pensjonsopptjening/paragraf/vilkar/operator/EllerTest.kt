package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
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
        avgjorelse1: Avgjorelse,
        avgjorelse2: Avgjorelse,
    ) {
        val ellerResultat = eller(
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse1),
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse2)
        )

        assertEquals(Avgjorelse.INVILGET, ellerResultat.avgjorelse)
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
        avgjorelse1: Avgjorelse,
        avgjorelse2: Avgjorelse,
        avgjorelse3: Avgjorelse,
    ) {
        val ellerResultat = eller(
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse1),
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse2),
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse3)
        )

        assertEquals(Avgjorelse.SAKSBEHANDLING, ellerResultat.avgjorelse)
    }

    @Test
    fun `Given All vilkar AVSLAG When evaluating eller Then AVSLAG`() {
        val ellerResultat = eller(
            returnAvgjorelse.vilkarsVurder(grunnlag = Avgjorelse.AVSLAG),
            returnAvgjorelse.vilkarsVurder(grunnlag = Avgjorelse.AVSLAG)
        )

        assertEquals(Avgjorelse.AVSLAG, ellerResultat.avgjorelse)
    }

    companion object {
        private val returnAvgjorelse = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            avgjorelsesFunksjon = fun(avgjorelse: Avgjorelse) = avgjorelse
        )
    }
}
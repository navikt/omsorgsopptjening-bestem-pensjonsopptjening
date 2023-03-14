package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Avgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator.Og.Companion.og
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OgTest {

    @Test
    fun `Given all vilkar is INVILGET When evaluating og Then INVILGET`() {
        val ogRegel = og(
            returnAvgjorelse.vilkarsVurder(grunnlag = Avgjorelse.INVILGET),
            returnAvgjorelse.vilkarsVurder(grunnlag = Avgjorelse.INVILGET)
        )

        kotlin.test.assertEquals(Avgjorelse.INVILGET, ogRegel.utfor().avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "SAKSBEHANDLING, SAKSBEHANDLING",
        "SAKSBEHANDLING, INVILGET",
        "INVILGET, SAKSBEHANDLING",
        "SAKSBEHANDLING, AVSLAG",
        "AVSLAG, SAKSBEHANDLING",
    )
    fun `Given at least one vilkar is SAKSBEHANDLING When evaluating og Then SAKSBEHANDLING`(
        avgjorelse1: Avgjorelse,
        avgjorelse2: Avgjorelse,
    ) {
        val ellerResultat = og(
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse1),
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse2)
        )

        Assertions.assertEquals(Avgjorelse.SAKSBEHANDLING, ellerResultat.utfor().avgjorelse)
    }

    @ParameterizedTest
    @CsvSource(
        "AVSLAG, INVILGET",
        "INVILGET, AVSLAG",
        "AVSLAG, AVSLAG",
    )
    fun `Given at least one vilkar with AVSLAG and no vilkar with SAKSBEHANDLING When evaluating og Then AVSLAG`(
        avgjorelse1: Avgjorelse,
        avgjorelse2: Avgjorelse,
    ) {
        val ellerResultat = og(
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse1),
            returnAvgjorelse.vilkarsVurder(grunnlag = avgjorelse2)
        )

        Assertions.assertEquals(Avgjorelse.AVSLAG, ellerResultat.utfor().avgjorelse)
    }



    companion object {
        private val returnAvgjorelse = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            avgjorelsesFunksjon = fun(avgjorelse: Avgjorelse) = avgjorelse
        )
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.operator

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Vilkar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.VilkarsInformasjon
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class EllerTest {

    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "true, false, true",
        "true, true, true",
        "false, false, false"
    )
    fun `Given an OR rule when evaluating the rule then return true if one rule is true`(
        boolean1: Boolean,
        boolean2: Boolean,
        forventetUtfall: Boolean
    ) {
        val ellerRegel = Eller.eller(
            returnGrunnlag.vilkarsVurder(grunnlag = boolean1),
            returnGrunnlag.vilkarsVurder(grunnlag = boolean2)
        )

        kotlin.test.assertEquals(forventetUtfall, ellerRegel.avgjorelse)
    }

    companion object {
        private val returnGrunnlag = Vilkar(
            vilkarsInformasjon = VilkarsInformasjon("test", "test", "test"),
            kalkulerAvgjorelse = fun(boolean: Boolean) = boolean
        )
    }
}
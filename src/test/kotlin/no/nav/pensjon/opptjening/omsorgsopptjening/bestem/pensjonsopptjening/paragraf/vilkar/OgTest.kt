package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OgTest {
    @ParameterizedTest
    @CsvSource(
        "true, true, true",
        "true, false, false",
        "false, true, false",
        "false, false, false"
    )
    fun `Given an AND rule when evaluating the rule then return true only if all rules are true`(
        boolean1: Boolean, boolean2: Boolean, forventetUtfall: Boolean
    ) {
        val ogRegel = Og.og(
            returnGrunnlag.vilkarsVurder(grunnlag = boolean1),
            returnGrunnlag.vilkarsVurder(grunnlag = boolean2)
        )

        kotlin.test.assertEquals(forventetUtfall, ogRegel.oppFyllerRegel)
    }

    companion object {
        private val returnGrunnlag = Vilkar(
            regelInformasjon = RegelInformasjon("test", "test", "test"),
            oppfyllerRegler = fun(boolean: Boolean) = boolean
        )
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OmsorgsyterErikkeOmsorgsmottakerTest {
    @Test
    fun `avslag hvis omsorgsyter er den samme som omsorgsmottaker`() {
        OmsorgsyterErikkeOmsorgsmottaker.vilkarsVurder(
            OmsorgsyterErikkeOmsorgsmottaker.Grunnlag(
                omsorgsyter = "far",
                omsorgsmottaker = "far"
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, it.utfall)
        }
    }

    @Test
    fun `innvilget hvis omsorgsyter ikke er den samme som omsorgsmottaker`() {
        OmsorgsyterErikkeOmsorgsmottaker.vilkarsVurder(
            OmsorgsyterErikkeOmsorgsmottaker.Grunnlag(
                omsorgsyter = "far",
                omsorgsmottaker = "sønn"
            )
        ).also {
            assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, it.utfall)
        }
    }
}
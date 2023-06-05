package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OmsorgsyterUnder70ArTest {
    @Test
    fun `should be innvilget when subject younger than 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterUnder70Ar().vilkarsVurder(
            OmsorgsyterOgOmsorgsårGrunnlag(
                omsorgsyter = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 69
            )
        )
        Assertions.assertInstanceOf(OmsorgsyterUnder70ArInnvilget::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject older than 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterUnder70Ar().vilkarsVurder(
            OmsorgsyterOgOmsorgsårGrunnlag(
                omsorgsyter = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 71
            )
        )
        Assertions.assertInstanceOf(OmsorgsyterUnder70ArAvslag::class.java, vilkarsVurdering.utfall).also {
            assertEquals(listOf(AvslagÅrsak.OMSORGSYTER_OVER_69), it.årsaker)
        }
    }

    @Test
    fun `should be avslag when subject is 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterUnder70Ar().vilkarsVurder(
            OmsorgsyterOgOmsorgsårGrunnlag(
                omsorgsyter = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 70
            )
        )
        Assertions.assertInstanceOf(OmsorgsyterUnder70ArAvslag::class.java, vilkarsVurdering.utfall).also {
            assertEquals(listOf(AvslagÅrsak.OMSORGSYTER_OVER_69), it.årsaker)
        }
    }
}
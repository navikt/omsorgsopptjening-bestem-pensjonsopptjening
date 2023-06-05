package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OmsorgsYterOver16ArTest {

    @Test
    fun `should be innvilget when subject older than 16`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsYterOver16Ar().vilkarsVurder(
            OmsorgsyterOgOmsorgsårGrunnlag(
                omsorgsyter = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 17
            )
        )
        assertInstanceOf(OmsorgsyterOver16ArInnvilget::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject younger than 16`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsYterOver16Ar().vilkarsVurder(
            OmsorgsyterOgOmsorgsårGrunnlag(
                omsorgsyter = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 15
            )
        )
        assertInstanceOf(OmsorgsyterOver16ArAvslag::class.java, vilkarsVurdering.utfall).also {
            assertEquals(it.årsaker, listOf(AvslagÅrsak.OMSORGSYTER_IKKE_OVER_16))
        }
    }

    @Test
    fun `should be avslag when subject is 16`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsYterOver16Ar().vilkarsVurder(
            OmsorgsyterOgOmsorgsårGrunnlag(
                omsorgsyter = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 16
            )
        )
        assertInstanceOf(OmsorgsyterOver16ArAvslag::class.java, vilkarsVurdering.utfall).also {
            assertEquals(listOf(AvslagÅrsak.OMSORGSYTER_IKKE_OVER_16), it.årsaker)
        }
    }
}
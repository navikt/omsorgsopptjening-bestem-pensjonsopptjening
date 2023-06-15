package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårTest {
    @Test
    fun `should be innvilget when subject younger than 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår().vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 69
            )
        )
        Assertions.assertInstanceOf(OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårInnvilget::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject older than 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår().vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 71
            )
        )
        Assertions.assertInstanceOf(OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårAvslag::class.java, vilkarsVurdering.utfall).also {
            assertEquals(listOf(AvslagÅrsak.OMSORGSYTER_ELDRE_ENN_69_VED_UTGANG_AV_OMSORGSÅR), it.årsaker)
        }
    }

    @Test
    fun `should be avslag when subject is 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsår().vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 70
            )
        )
        Assertions.assertInstanceOf(OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårAvslag::class.java, vilkarsVurdering.utfall).also {
            assertEquals(listOf(AvslagÅrsak.OMSORGSYTER_ELDRE_ENN_69_VED_UTGANG_AV_OMSORGSÅR), it.årsaker)
        }
    }
}
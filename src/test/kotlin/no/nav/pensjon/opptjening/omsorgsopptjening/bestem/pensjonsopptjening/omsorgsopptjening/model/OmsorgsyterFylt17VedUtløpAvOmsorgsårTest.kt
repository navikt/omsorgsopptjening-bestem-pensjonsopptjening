package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OmsorgsyterFylt17VedUtløpAvOmsorgsårTest {

    private val fnr = "01058512345"
    private val fødselsår = 2000

    @Test
    fun `should be innvilget when subject has turned 17 before omsorgsår`() {
        val vilkarsVurdering = OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = fnr,
                    fodselsAr = fødselsår
                ),
                omsorgsAr = fødselsår + 18
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.EnkeltParagraf::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be innvilget when subject turns 17 in omsorgsår`() {
        val vilkarsVurdering = OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = fnr,
                    fodselsAr = fødselsår
                ),
                omsorgsAr = fødselsår + 17
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.EnkeltParagraf::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject has not turned 17`() {
        val vilkarsVurdering = OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = fnr,
                    fodselsAr = fødselsår
                ),
                omsorgsAr = fødselsår + 16
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Avslag.EnkeltParagraf::class.java, vilkarsVurdering.utfall).also {
            assertEquals(
                setOf(
                    Lovhenvisning.FYLLER_17_AR
                ),
                it.lovhenvisning
            )
        }
    }
}
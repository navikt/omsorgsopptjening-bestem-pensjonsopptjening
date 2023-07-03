package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OmsorgsyterErFylt17VedUtløpAvOmsorgsårTest {

    private val fnr = "01058512345"
    private val fødselsår = 2000

    @Test
    fun `should be innvilget when subject has turned 17 before omsorgsår`() {
        val vilkarsVurdering = OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = fnr,
                    fodselsAr = fødselsår
                ),
                omsorgsAr = fødselsår + 18
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be innvilget when subject turns 17 in omsorgsår`() {
        val vilkarsVurdering = OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = fnr,
                    fodselsAr = fødselsår
                ),
                omsorgsAr = fødselsår + 17
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject has not turned 17`() {
        val vilkarsVurdering = OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = fnr,
                    fodselsAr = fødselsår
                ),
                omsorgsAr = fødselsår + 16
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall).also {
            assertEquals(
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
                ),
                it.henvisninger
            )
        }
    }
}
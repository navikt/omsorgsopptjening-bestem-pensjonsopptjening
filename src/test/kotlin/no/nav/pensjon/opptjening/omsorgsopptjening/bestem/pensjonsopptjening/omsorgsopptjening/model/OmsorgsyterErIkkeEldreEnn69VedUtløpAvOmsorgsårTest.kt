package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsårTest {
    @Test
    fun `should be innvilget when subject younger than 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 69
            )
        )
        Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject older than 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 71
            )
        )
        Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall).also {
            assertEquals(
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
                ),
                it.henvisninger
            )
        }
    }

    @Test
    fun `should be avslag when subject is 70 years`() {
        val årstall = 2000
        val vilkarsVurdering = OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = PersonMedFødselsår(
                    fnr = "12345678910",
                    fodselsAr = årstall
                ),
                omsorgsAr = årstall + 70
            )
        )
        Assertions.assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall).also {
            assertEquals(
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
                ),
                it.henvisninger
            )
        }
    }
}
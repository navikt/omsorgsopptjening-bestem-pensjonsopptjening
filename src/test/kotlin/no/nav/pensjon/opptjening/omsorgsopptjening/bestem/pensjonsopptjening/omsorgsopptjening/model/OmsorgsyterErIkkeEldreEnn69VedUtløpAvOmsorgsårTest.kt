package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertEquals

class OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsårTest {

    private val årstall = LocalDate.of(2000, Month.JANUARY, 1)
    private val person = Person(
        fnr = "12345678910",
        fødselsdato = årstall,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList())
    )

    @Test
    fun `should be innvilget when subject younger than 70 years`() {

        val vilkarsVurdering = OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = årstall.plusYears(69).year
            )
        )
        Assertions.assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject older than 70 years`() {
        val vilkarsVurdering = OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = årstall.plusYears(71).year
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
        val vilkarsVurdering = OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = årstall.plusYears(70).year
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
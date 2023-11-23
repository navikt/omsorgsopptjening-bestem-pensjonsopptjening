package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class OmsorgsyterOppfyllerAlderskravTest {

    private val fnr = "01058512345"
    private val fødselsår = LocalDate.of(2000, Month.JANUARY, 1)

    private val person = Person(
        fødselsdato = fødselsår,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList()),
        identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende(fnr)))
    )

    @Test
    fun `should be innvilget when subject has turned 17 before omsorgsår`() {
        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            AldersvurderingsGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(18).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be innvilget when subject turns 17 in omsorgsår`() {
        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            AldersvurderingsGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(17).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject has not turned 17`() {
        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            AldersvurderingsGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(16).year
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

    @Test
    fun `should be innvilget when subject younger than 70 years`() {

        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            AldersvurderingsGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(69).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `should be avslag when subject older than 70 years`() {
        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            AldersvurderingsGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(71).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall).also {
            kotlin.test.assertEquals(
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
                ),
                it.henvisninger
            )
        }
    }

    @Test
    fun `should be avslag when subject is 70 years`() {
        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            AldersvurderingsGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(70).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall).also {
            kotlin.test.assertEquals(
                setOf(
                    JuridiskHenvisning.Folketrygdloven_Kap_20_Paragraf_8_Andre_Ledd
                ),
                it.henvisninger
            )
        }
    }
}
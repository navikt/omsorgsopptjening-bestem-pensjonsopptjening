package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model


import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class OmsorgsmottakerOppfyllerAlderskravTest {

    private val fnr = "01058512345"
    private val fødselsår = LocalDate.of(2000, Month.JANUARY, 1)

    private val person = Person(
        fnr = fnr,
        fødselsdato = fødselsår,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList())
    )

    @Test
    fun `oppfyller alderskrav for barnetrygd dersom barnet er mellom 0 og 5 år i omsorgsåret`() {
        val vilkarsVurdering0år = OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = fødselsår.year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering0år.utfall)

        val vilkarsVurdering5år = OmsorgsmottakerOppfyllerAlderskravForBarnetrygd.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(5).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering5år.utfall)
    }

    @Test
    fun `oppfyller ikke alderskrav for barnetrygd dersom barnet er eldre en 5 i omsorgsåret`() {
        val vilkarsVurdering = OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(6).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall)
    }

    @Test
    fun `oppfyller alderskrav for hjelpestønad dersom barnet er mellom 6 og 18 år i omsorgsåret`() {
        val vilkarsVurdering0år = OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(6).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering0år.utfall)

        val vilkarsVurdering5år = OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(18).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Innvilget.Vilkår::class.java, vilkarsVurdering5år.utfall)
    }

    @Test
    fun `oppfyller ikke alderskrav for hjelpestønad dersom barnet er eldre en 18 i omsorgsåret`() {
        val vilkarsVurdering = OmsorgsmottakerOppfyllerAlderskravForHjelpestønad.vilkarsVurder(
            PersonOgOmsorgsårGrunnlag(
                person = person,
                omsorgsAr = fødselsår.plusYears(19).year
            )
        )
        assertInstanceOf(VilkårsvurderingUtfall.Avslag.Vilkår::class.java, vilkarsVurdering.utfall)
    }
}
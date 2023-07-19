package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class OgTest {

    private val omsorgsår = 2020
    private val fodselAvslag = LocalDate.of(omsorgsår - 10, Month.JANUARY, 1)
    private val fodselInnvilget = LocalDate.of(omsorgsår - 20, Month.JANUARY, 1)

    @Test
    fun `all avslag is avslag`() {
        og(
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
        ).also {
            assertInstanceOf(OgAvslått::class.java, it.utfall)
        }
    }

    @Test
    fun `one avslag is avslag`() {
        og(
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
        ).also {
            assertInstanceOf(OgAvslått::class.java, it.utfall)
        }
    }

    @Test
    fun `all innvilget is innvilget`() {
        og(
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
        ).also {
            assertInstanceOf(OgInnvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `nested og with all avslag is avslag`() {
        og(
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                    grunnlag = PersonOgOmsorgsårGrunnlag(
                        person = Person(fnr = "1", fødselsdato = fodselAvslag),
                        omsorgsAr = omsorgsår
                    )
                ),
            )
        ).also {
            assertInstanceOf(OgAvslått::class.java, it.utfall)
        }
    }

    @Test
    fun `nested og with all innvilget is avslag`() {
        og(
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                    grunnlag = PersonOgOmsorgsårGrunnlag(
                        person = Person(fnr = "1", fødselsdato = fodselInnvilget),
                        omsorgsAr = omsorgsår
                    )
                ),
            )
        ).also {
            assertInstanceOf(OgAvslått::class.java, it.utfall)
        }
    }

    @Test
    fun `nested all innvilget is innvilget`() {
        og(
            OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = Person(fnr = "1", fødselsdato = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterErFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                    grunnlag = PersonOgOmsorgsårGrunnlag(
                        person = Person(fnr = "1", fødselsdato = fodselInnvilget),
                        omsorgsAr = omsorgsår
                    )
                ),
            )
        ).also {
            assertInstanceOf(OgInnvilget::class.java, it.utfall)
        }
    }
}
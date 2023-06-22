package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Eller.Companion.eller
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class EllerTest {

    private val omsorgsår = 2020
    private val fodselAvslag = omsorgsår - 10
    private val fodselInnvilget = omsorgsår - 20

    @Test
    fun `all avslag is avslag`() {
        eller(
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
        ).also {
            assertInstanceOf(EllerAvslått::class.java, it.utfall)
        }
    }

    @Test
    fun `one innvilget is innvilget`() {
        eller(
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
        ).also {
            assertInstanceOf(EllerInnvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `all innvilget is innvilget`() {
        eller(
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
        ).also {
            assertInstanceOf(EllerInnvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `nested eller with all avslag is avslag`() {
        eller(
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            eller(
                OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                    grunnlag = PersonOgOmsorgsårGrunnlag(
                        person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                        omsorgsAr = omsorgsår
                    )
                )
            )
        ).also {
            assertInstanceOf(EllerAvslått::class.java, it.utfall)
        }
    }

    @Test
    fun `nested eller with all innvilget is innvilget`() {
        eller(
            OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                grunnlag = PersonOgOmsorgsårGrunnlag(
                    person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            eller(
                OmsorgsyterFylt17VedUtløpAvOmsorgsår.vilkarsVurder(
                    grunnlag = PersonOgOmsorgsårGrunnlag(
                        person = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                        omsorgsAr = omsorgsår
                    )
                )
            )
        ).also {
            assertInstanceOf(EllerInnvilget::class.java, it.utfall)
        }
    }

}
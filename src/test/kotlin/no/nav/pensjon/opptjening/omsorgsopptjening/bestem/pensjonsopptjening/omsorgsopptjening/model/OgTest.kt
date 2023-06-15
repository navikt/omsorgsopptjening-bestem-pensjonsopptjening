package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Og.Companion.og
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.PersonMedFødselsår
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class OgTest {

    private val omsorgsår = 2020
    private val fodselAvslag = omsorgsår - 10
    private val fodselInnvilget = omsorgsår - 20

    @Test
    fun `all avslag is avslag`() {
        og(
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
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
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
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
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
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
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                    grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                        omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
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
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselAvslag),
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                    grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                        omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
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
            OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                    omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterFylt17VedUtløpAvOmsorgsår().vilkarsVurder(
                    grunnlag = OmsorgsyterOgOmsorgsårGrunnlag(
                        omsorgsyter = PersonMedFødselsår(fnr = "1", fodselsAr = fodselInnvilget),
                        omsorgsAr = omsorgsår
                    )
                ),
            )
        ).also {
            assertInstanceOf(OgInnvilget::class.java, it.utfall)
        }
    }
}
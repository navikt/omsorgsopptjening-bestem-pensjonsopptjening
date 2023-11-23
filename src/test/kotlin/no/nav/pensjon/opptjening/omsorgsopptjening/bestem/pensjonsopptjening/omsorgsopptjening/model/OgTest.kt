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
    private val personAvslag = Person(
        fødselsdato = fodselAvslag,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList()),
        identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("1")))
    )
    private val personInnvilget =
        Person(
            fødselsdato = fodselInnvilget,
            dødsdato = null,
            familierelasjoner = Familierelasjoner(emptyList()),
            identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("1")))
        )

    @Test
    fun `all avslag is avslag`() {
        og(
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
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
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personInnvilget,
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
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personInnvilget,
                    omsorgsAr = omsorgsår
                )
            ),
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personInnvilget,
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
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                    grunnlag = AldersvurderingsGrunnlag(
                        person = personAvslag,
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
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                    grunnlag = AldersvurderingsGrunnlag(
                        person = personInnvilget,
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
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personInnvilget,
                    omsorgsAr = omsorgsår
                )
            ),
            og(
                OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                    grunnlag = AldersvurderingsGrunnlag(
                        person = personInnvilget,
                        omsorgsAr = omsorgsår
                    )
                ),
            )
        ).also {
            assertInstanceOf(OgInnvilget::class.java, it.utfall)
        }
    }
}
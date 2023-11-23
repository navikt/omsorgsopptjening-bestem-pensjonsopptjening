package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Eller.Companion.eller

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class EllerTest {

    private val omsorgsår = 2020
    private val fodselAvslag = LocalDate.of(omsorgsår - 10, Month.JANUARY, 1)
    private val fodselInnvilget = LocalDate.of(omsorgsår - 20, Month.JANUARY, 1)
    private val personAvslag = Person(
        fødselsdato = fodselAvslag,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList()),
        identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("1")))
    )
    private val personInnvilget = Person(
        fødselsdato = fodselInnvilget,
        dødsdato = null,
        familierelasjoner = Familierelasjoner(emptyList()),
        identhistorikk = IdentHistorikk(setOf(Ident.FolkeregisterIdent.Gjeldende("1")))
    )

    @Test
    fun `all avslag is avslag`() {
        eller(
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
            assertInstanceOf(EllerAvslått::class.java, it.utfall)
        }
    }


    @Test
    fun `one innvilget is innvilget`() {
        eller(
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
            assertInstanceOf(EllerInnvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `all innvilget is innvilget`() {
        eller(
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
            assertInstanceOf(EllerInnvilget::class.java, it.utfall)
        }
    }

    @Test
    fun `nested eller with all avslag is avslag`() {
        eller(
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
                    omsorgsAr = omsorgsår
                )
            ),
            eller(
                OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                    grunnlag = AldersvurderingsGrunnlag(
                        person = personAvslag,
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
            OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                grunnlag = AldersvurderingsGrunnlag(
                    person = personAvslag,
                    omsorgsAr = omsorgsår
                )
            ),
            eller(
                OmsorgsyterOppfyllerAlderskrav.vilkarsVurder(
                    grunnlag = AldersvurderingsGrunnlag(
                        person = personInnvilget,
                        omsorgsAr = omsorgsår
                    )
                )
            )
        ).also {
            assertInstanceOf(EllerInnvilget::class.java, it.utfall)
        }
    }

}
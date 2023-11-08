package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Pensjonspoeng
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottakerTest {

    @Test
    fun `skal ikke sendes for barnetrygd`() {
        assertThat(
            HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
                hentPensjonspoengForOmsorgsopptjening = { _,_,_ ->
                    Pensjonspoeng.Omsorg(omsorgsår, 0.0, DomainOmsorgstype.BARNETRYGD)
                },
                hentPensjonspoengForInntekt = { _,_ ->
                    Pensjonspoeng.Inntekt(omsorgsår, 0.0)
                }
            ).get(
                omsorgsyter = far,
                omsorgsmottaker = omsorgsmottaker,
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
                omsorgsAr = omsorgsår
            )
        ).isEqualTo(Brevopplysninger.Ingen)
    }

    @Test
    fun `send hvis omsorgsyter ikke har pensjonspoeng forrige år`() {
        assertThat(
            HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
                hentPensjonspoengForOmsorgsopptjening = { _,_,_ ->
                    Pensjonspoeng.Omsorg(omsorgsår - 1, 0.0, DomainOmsorgstype.HJELPESTØNAD)
                },
                hentPensjonspoengForInntekt = { _,_ ->
                    Pensjonspoeng.Inntekt(omsorgsår, 0.0)
                }
            ).get(
                omsorgsyter = far,
                omsorgsmottaker = omsorgsmottaker,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
                omsorgsAr = omsorgsår
            )
        ).isEqualTo(Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR))
    }

    @Test
    fun `send hvis omsorgsyter ikke er forelder til omsorgsmottaker`() {
        assertThat(
            HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
                hentPensjonspoengForOmsorgsopptjening = { _,_,_ ->
                    Pensjonspoeng.Omsorg(omsorgsår - 1, 3.5, DomainOmsorgstype.HJELPESTØNAD)
                },
                hentPensjonspoengForInntekt = { _,_ ->
                    Pensjonspoeng.Inntekt(omsorgsår, 0.0)
                }
            ).get(
                omsorgsyter = onkel,
                omsorgsmottaker = omsorgsmottaker,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
                omsorgsAr = omsorgsår
            )
        ).isEqualTo(Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER))
    }

    @Test
    fun `send hvis far er omsorgsyter og mor har lavere pensjonspoeng enn 3,5`() {
        assertThat(
            HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
                hentPensjonspoengForOmsorgsopptjening = { _,_,_ ->
                    Pensjonspoeng.Omsorg(omsorgsår - 1, 3.5, DomainOmsorgstype.HJELPESTØNAD)
                },
                hentPensjonspoengForInntekt = { _,_ ->
                    Pensjonspoeng.Inntekt(omsorgsår, 2.8)
                }
            ).get(
                omsorgsyter = far,
                omsorgsmottaker = omsorgsmottaker,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
                omsorgsAr = omsorgsår
            )
        ).isEqualTo(Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG))
    }

    @Test
    fun `send hvis mor er omsorgsyter og far har lavere pensjonspoeng enn 3,5`() {
        assertThat(
            HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
                hentPensjonspoengForOmsorgsopptjening = { _,_,_ ->
                    Pensjonspoeng.Omsorg(omsorgsår - 1, 3.5, DomainOmsorgstype.HJELPESTØNAD)
                },
                hentPensjonspoengForInntekt = { _,_ ->
                    Pensjonspoeng.Inntekt(omsorgsår, 2.8)
                }
            ).get(
                omsorgsyter = mor,
                omsorgsmottaker = omsorgsmottaker,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
                omsorgsAr = omsorgsår
            )
        ).isEqualTo(Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG))
    }

    @Test
    fun `send hvis foreldre er ukjent`() {
        assertThat(
            HentBrevopplysningForInfobrevOmsorgsyterForHjelpestønadsmottaker(
                hentPensjonspoengForOmsorgsopptjening = { _,_,_ ->
                    Pensjonspoeng.Omsorg(omsorgsår - 1, 3.5, DomainOmsorgstype.HJELPESTØNAD)
                },
                hentPensjonspoengForInntekt = { _,_ ->
                    Pensjonspoeng.Inntekt(omsorgsår, 2.8)
                }
            ).get(
                omsorgsyter = onkel,
                omsorgsmottaker = omsorgsmottakerUkjenteForeldre,
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
                omsorgsAr = omsorgsår
            )
        ).isEqualTo(Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker(BrevÅrsak.FORELDRE_ER_UKJENT))
    }

    private val omsorgsår: Int = 2020

    private val omsorgsmottaker = Person(
        fnr = "barn",
        fødselsdato = LocalDate.of(2018, Month.JANUARY, 1),
        dødsdato = null,
        familierelasjoner = Familierelasjoner(
            relasjoner = listOf(
                Familierelasjon(
                    ident = "far",
                    relasjon = Familierelasjon.Relasjon.FAR
                ),
                Familierelasjon(
                    ident = "mor",
                    relasjon = Familierelasjon.Relasjon.MOR
                )
            )
        )
    )

    private val omsorgsmottakerUkjenteForeldre = Person(
        fnr = "barn",
        fødselsdato = LocalDate.of(2018, Month.JANUARY, 1),
        dødsdato = null,
        familierelasjoner = Familierelasjoner(
            relasjoner = listOf()
        )
    )


    private val far = Person(
        fnr = "far",
        fødselsdato = LocalDate.of(1960, Month.JANUARY, 1),
        dødsdato = null,
        familierelasjoner = Familierelasjoner(
            relasjoner = listOf(
                Familierelasjon(
                    ident = "barn",
                    relasjon = Familierelasjon.Relasjon.BARN
                ),
            )
        )
    )

    private val mor = Person(
        fnr = "mor",
        fødselsdato = LocalDate.of(1960, Month.JANUARY, 1),
        dødsdato = null,
        familierelasjoner = Familierelasjoner(
            relasjoner = listOf(
                Familierelasjon(
                    ident = "barn",
                    relasjon = Familierelasjon.Relasjon.BARN
                ),
            )
        )
    )

    private val onkel = Person(
        fnr = "onkel",
        fødselsdato = LocalDate.of(1960, Month.JANUARY, 1),
        dødsdato = null,
        familierelasjoner = Familierelasjoner(
            relasjoner = listOf()
        )
    )
}
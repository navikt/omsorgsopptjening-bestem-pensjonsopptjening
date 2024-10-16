package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AntallMånederRegel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ident
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.IdentHistorikk
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.juni
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.mars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.util.UUID
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

private const val mor = "12345678910"
private const val jente = "01122012345"
private const val far = "04010012797"
private const val gutt = "07081812345"

class OmsorgsopptjeningsgrunnlagServiceImplTest {

    private val personOppslag: PersonOppslag = mock()
    private val medlemskapsUnntaktOppslag: MedlemskapsUnntakOppslag = mock()

    private val service = OmsorgsopptjeningsgrunnlagServiceImpl(
        personOppslag = personOppslag,
        medlemskapsUnntakOppslag = medlemskapsUnntaktOppslag,
    )

    @BeforeEach
    fun beforeEach() {
        whenever(personOppslag.hentPerson(mor)).thenReturn(
            lagPerson(
                mor,
                LocalDate.of(1980, Month.JANUARY, 1)
            )
        )
        whenever(personOppslag.hentPerson(jente)).thenReturn(
            lagPerson(
                jente,
                LocalDate.of(2020, Month.DECEMBER, 1)
            )
        )
        whenever(personOppslag.hentPerson(far)).thenReturn(
            lagPerson(
                far,
                LocalDate.of(1980, Month.JANUARY, 1)
            )
        )
        whenever(personOppslag.hentPerson(gutt)).thenReturn(
            lagPerson(
                gutt,
                LocalDate.of(2018, Month.AUGUST, 7)
            )
        )

        whenever(medlemskapsUnntaktOppslag.hentUnntaksperioder(any(), any(), any())).thenReturn(
            Medlemskapsunntak(
                emptySet(),
                emptySet(),
                ""
            )
        )
    }

    @Test
    fun `håndtering av flere omsorgsytere med flere omsorgsmottakere`() {
        val omsorgsgrunnlag = service.lagOmsorgsopptjeningsgrunnlag(
            PersongrunnlagMelding.Mottatt(
                id = UUID.randomUUID(),
                opprettet = Instant.now(),
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = mor,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = mor,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = jente,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.FEBRUARY),
                                    tom = YearMonth.of(2021, Month.MARCH),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.APRIL),
                                    tom = YearMonth.of(2021, Month.MAY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JUNE),
                                    tom = YearMonth.of(2021, Month.JULY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 2000,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.DECEMBER),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2022, Month.JUNE),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 2000,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = far,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = jente,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            )
        )
        val perBarn = omsorgsgrunnlag.groupBy { it.omsorgsmottaker.fnr }

        assertThat(omsorgsgrunnlag).hasSize(6)
        assertThat(omsorgsgrunnlag.first().omsorgsmottaker.fnr).isEqualTo(gutt) //eldst først
        assertThat(omsorgsgrunnlag.last().omsorgsmottaker.fnr).isEqualTo(jente) //eldst først

        perBarn[jente]!!.also {
            assertThat(it).hasSize(3)
            assertThat(it.map { it.omsorgsAr }).containsAll(setOf(2020, 2021, 2021))
            it.single { it.omsorgsAr == 2020 }.also { grl ->
                assertThat(grl).isInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember::class.java)
                assertThat(grl.omsorgsyter.fnr).isEqualTo(mor)
                assertThat(grl.omsorgsmottaker.fnr).isEqualTo(jente)
                assertThat(grl.omsorgsAr).isEqualTo(2020)
                assertThat(grl.omsorgstype).isEqualTo(DomainOmsorgskategori.BARNETRYGD)
                assertThat(grl.antallMånederRegel()).isEqualTo(AntallMånederRegel.FødtIOmsorgsår)
                grl.forMottarBarnetrygd().also {
                    assertThat(it.omsorgsytersUtbetalingsmåneder.alle()).isEqualTo(år(2021).alleMåneder())
                    assertThat(it.omsorgsytersUtbetalingsmåneder.antall()).isEqualTo(12)
                    assertThat(it.omsorgstype).isEqualTo(DomainOmsorgskategori.BARNETRYGD)
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtIOmsorgsår)
                }
                grl.forFamilierelasjon().also {
                    assertThat(it.omsorgsyter).isEqualTo(mor)
                    assertThat(it.omsorgsytersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                    assertThat(it.omsorgsmottaker).isEqualTo(jente)
                    assertThat(it.omsorgsmottakersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                }
                grl.forAldersvurderingOmsorgsyter().also {
                    assertThat(it.omsorgsAr).isEqualTo(2020)
                    assertThat(it.person.fnr).isEqualTo(mor)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(1980, Month.JANUARY, 1))
                    assertThat(it.alder).isEqualTo(40)
                }
                grl.forAldersvurderingOmsorgsmottaker().also {
                    assertThat(it.omsorgsAr).isEqualTo(2020)
                    assertThat(it.person.fnr).isEqualTo(jente)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(2020, Month.DECEMBER, 1))
                    assertThat(it.alder).isEqualTo(0)
                }
                grl.forTilstrekkeligOmsorgsarbeid().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtIOmsorgsår)
                    assertThat(it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()).isEqualTo(år(2021).alleMåneder())
                    assertThat(it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.antall()).isEqualTo(12)
                    assertThat(it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.omsorgstype()).isEqualTo(
                        DomainOmsorgskategori.BARNETRYGD
                    )
                }
                grl.forGyldigOmsorgsarbeidPerOmsorgsyter().also {
                    assertThat(it.omsorgsyter).isEqualTo(mor)
                    it.data.single { it.omsorgsyter == mor }.also {
                        assertThat(it.omsorgsyter).isEqualTo(mor)
                        assertThat(it.omsorgsmottaker).isEqualTo(jente)
                        assertThat(it.omsorgsår).isEqualTo(2020)
                        assertThat(it.omsorgsmåneder.alleMåneder()).isEqualTo(år(2021).alleMåneder())
                    }
                    it.data.single { it.omsorgsyter == far }.also {
                        assertThat(it.omsorgsyter).isEqualTo(far)
                        assertThat(it.omsorgsmottaker).isEqualTo(jente)
                        assertThat(it.omsorgsår).isEqualTo(2020)
                        assertThat(it.omsorgsmåneder.alleMåneder()).isEqualTo(år(2021).alleMåneder())
                    }
                }
                grl.forMedlemskapIFolketrygden().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtIOmsorgsår)
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.ikkeMedlem).isEmpty()
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.pliktigEllerFrivillig).isEmpty()
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.rådata).isEqualTo("")
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.rådata).isEqualTo("")
                    assertThat(it.omsorgsytersOmsorgsmåneder.alle()).isEqualTo(år(2021).alleMåneder())
                    assertThat(it.omsorgsytersOmsorgsmåneder.antall()).isEqualTo(12)
                    assertThat(it.landstilknytningMåneder.alle()).isEqualTo(år(2021).alleMåneder())
                }
            }
            it.single { it.omsorgsAr == 2021 }.also {
                assertThat(it).isInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java)
            }
            it.single { it.omsorgsAr == 2022 }.also {
                assertThat(it).isInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java)
            }
        }

        perBarn[gutt]!!.also {
            assertThat(it).hasSize(3)
            assertThat(it.map { it.omsorgsAr }).containsAll(setOf(2020, 2021, 2021))
            it.single { it.omsorgsAr == 2021 }.also { grl ->
                assertThat(grl).isInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java)
                assertThat(grl.omsorgsyter.fnr).isEqualTo(mor)
                assertThat(grl.omsorgsmottaker.fnr).isEqualTo(gutt)
                assertThat(grl.omsorgsAr).isEqualTo(2021)
                assertThat(grl.omsorgstype).isEqualTo(DomainOmsorgskategori.BARNETRYGD)
                assertThat(grl.antallMånederRegel()).isEqualTo(AntallMånederRegel.FødtUtenforOmsorgsår)
                grl.forMottarBarnetrygd().also {
                    assertThat(it.omsorgsytersUtbetalingsmåneder.alle()).isEqualTo(
                        setOf(
                            februar(2021),
                            mars(2021),
                            april(2021),
                            mai(2021),
                            juni(2021),
                            juli(2021),
                            desember(2021)
                        )
                    )
                    assertThat(it.omsorgsytersUtbetalingsmåneder.antall()).isEqualTo(7)
                    assertThat(it.omsorgstype).isEqualTo(DomainOmsorgskategori.BARNETRYGD)
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtUtenforOmsorgsår)
                }
                grl.forFamilierelasjon().also {
                    assertThat(it.omsorgsyter).isEqualTo(mor)
                    assertThat(it.omsorgsytersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                    assertThat(it.omsorgsmottaker).isEqualTo(gutt)
                    assertThat(it.omsorgsmottakersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                }
                grl.forAldersvurderingOmsorgsyter().also {
                    assertThat(it.omsorgsAr).isEqualTo(2021)
                    assertThat(it.person.fnr).isEqualTo(mor)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(1980, Month.JANUARY, 1))
                    assertThat(it.alder).isEqualTo(41)
                }
                grl.forAldersvurderingOmsorgsmottaker().also {
                    assertThat(it.omsorgsAr).isEqualTo(2021)
                    assertThat(it.person.fnr).isEqualTo(gutt)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(2018, Month.AUGUST, 7))
                    assertThat(it.alder).isEqualTo(3)
                }
                grl.forTilstrekkeligOmsorgsarbeid().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtUtenforOmsorgsår)
                    assertThat(it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.alle()).isEqualTo(
                        setOf(
                            februar(2021),
                            mars(2021),
                            april(2021),
                            mai(2021),
                            desember(2021)
                        )
                    )
                    assertThat(it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.antall()).isEqualTo(5)
                    assertThat(it.omsorgsytersOmsorgsmånederForOmsorgsmottaker.omsorgstype()).isEqualTo(
                        DomainOmsorgskategori.BARNETRYGD
                    )
                }
                grl.forGyldigOmsorgsarbeidPerOmsorgsyter().also {
                    assertThat(it.omsorgsyter).isEqualTo(mor)
                    it.data.single { it.omsorgsyter == mor }.also {
                        assertThat(it.omsorgsyter).isEqualTo(mor)
                        assertThat(it.omsorgsmottaker).isEqualTo(gutt)
                        assertThat(it.omsorgsår).isEqualTo(2021)
                        assertThat(it.omsorgsmåneder.alleMåneder()).isEqualTo(
                            setOf(
                                februar(2021),
                                mars(2021),
                                april(2021),
                                mai(2021),
                                desember(2021)
                            )
                        )
                    }
                    it.data.single { it.omsorgsyter == far }.also {
                        assertThat(it.omsorgsyter).isEqualTo(far)
                        assertThat(it.omsorgsmottaker).isEqualTo(gutt)
                        assertThat(it.omsorgsår).isEqualTo(2021)
                        assertThat(it.omsorgsmåneder.alleMåneder()).isEqualTo(år(2021).alleMåneder())
                    }
                }
                grl.forMedlemskapIFolketrygden().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtUtenforOmsorgsår)
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.ikkeMedlem).isEmpty()
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.pliktigEllerFrivillig).isEmpty()
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.rådata).isEqualTo("")
                    assertThat(it.medlemskapsgrunnlag.medlemskapsunntak.rådata).isEqualTo("")
                    assertThat(it.omsorgsytersOmsorgsmåneder.alle()).isEqualTo(
                        setOf(
                            februar(2021),
                            mars(2021),
                            april(2021),
                            mai(2021),
                            desember(2021)
                        )
                    )
                    assertThat(it.omsorgsytersOmsorgsmåneder.antall()).isEqualTo(5)
                    assertThat(it.landstilknytningMåneder.alle()).isEqualTo(
                        setOf(
                            februar(2021),
                            mars(2021),
                            april(2021),
                            mai(2021),
                            juni(2021),
                            juli(2021),
                            desember(2021)
                        )
                    )
                }
            }
            it.single { it.omsorgsAr == 2020 }.also {
                assertThat(it).isInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java)
            }
            it.single { it.omsorgsAr == 2022 }.also {
                assertThat(it).isInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java)
            }
        }
    }


    private fun lagPerson(fnr: String, fødselsdato: LocalDate): Person {
        return Person(
            fødselsdato = fødselsdato,
            dødsdato = null,
            familierelasjoner = Familierelasjoner(
                relasjoner = listOf()
            ),
            identhistorikk = IdentHistorikk(
                identer = setOf(Ident.FolkeregisterIdent.Gjeldende(fnr))
            )
        )
    }
}
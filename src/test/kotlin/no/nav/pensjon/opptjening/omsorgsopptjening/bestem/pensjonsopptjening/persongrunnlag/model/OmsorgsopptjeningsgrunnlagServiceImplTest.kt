package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AntallMånederRegel
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ident
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.IdentHistorikk
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelseinformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.april
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.år
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse.YtelseOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
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

class OmsorgsopptjeningsgrunnlagServiceImplTest {

    private val personOppslag: PersonOppslag = mock()
    private val medlemskapsUnntaktOppslag: MedlemskapsUnntakOppslag = mock()
    private val ytelseOppslag: YtelseOppslag = mock()

    private val mor = lagPerson(
        "12345678910",
        LocalDate.of(1980, Month.JANUARY, 1)
    )
    private val jente = lagPerson(
        "01122012345",
        LocalDate.of(2020, Month.DECEMBER, 1)
    )
    private val far = lagPerson(
        "04010012797",
        LocalDate.of(1980, Month.JANUARY, 1)
    )
    private val gutt = lagPerson(
        "07081812345",
        LocalDate.of(2018, Month.AUGUST, 7)
    )
    private val ungdom = lagPerson(
        "03041212345",
        LocalDate.of(2012, Month.MARCH, 4)
    )

    private val service = OmsorgsopptjeningsgrunnlagServiceImpl(
        personOppslag = personOppslag,
        medlemskapsUnntakOppslag = medlemskapsUnntaktOppslag,
        ytelseOppslag = ytelseOppslag,
    )

    @BeforeEach
    fun beforeEach() {
        whenever(personOppslag.hentPerson(mor.fnr)).thenReturn(mor)
        whenever(personOppslag.hentPerson(jente.fnr)).thenReturn(jente)
        whenever(personOppslag.hentPerson(far.fnr)).thenReturn(far)
        whenever(personOppslag.hentPerson(gutt.fnr)).thenReturn(gutt)
        whenever(personOppslag.hentPerson(ungdom.fnr)).thenReturn(ungdom)

        whenever(medlemskapsUnntaktOppslag.hentUnntaksperioder(any(), any(), any())).thenReturn(
            Medlemskapsunntak(
                emptySet(),
                emptySet(),
                ""
            )
        )

        whenever(ytelseOppslag.hentLøpendeAlderspensjon(any(), any(), any())).thenReturn(
            Ytelseinformasjon(
                emptySet(),
                ""
            )
        )
        whenever(ytelseOppslag.hentLøpendeUføretrygd(any(), any(), any())).thenReturn(Ytelseinformasjon(emptySet(), ""))
    }

    @Test
    fun `håndtering av flere omsorgsytere med flere omsorgsmottakere`() {
        val omsorgsgrunnlag = service.lagOmsorgsopptjeningsgrunnlag(
            PersongrunnlagMelding.Mottatt(
                id = UUID.randomUUID(),
                opprettet = Instant.now(),
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = mor.fnr,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = mor.fnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = jente.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.FEBRUARY),
                                    tom = YearMonth.of(2021, Month.MARCH),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.APRIL),
                                    tom = YearMonth.of(2021, Month.MAY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JUNE),
                                    tom = YearMonth.of(2021, Month.JULY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 2000,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.DECEMBER),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2022, Month.JUNE),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 2000,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = far.fnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = jente.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = YearMonth.of(2022, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = gutt.fnr,
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
        assertThat(omsorgsgrunnlag.first().omsorgsmottaker).isEqualTo(gutt) //eldst først
        assertThat(omsorgsgrunnlag.last().omsorgsmottaker).isEqualTo(jente) //eldst først

        perBarn[jente.fnr]!!.also {
            assertThat(it).hasSize(3)
            assertThat(it.map { it.omsorgsAr }).containsAll(setOf(2020, 2021, 2021))
            it.single { it.omsorgsAr == 2020 }.also { grl ->
                assertThat(grl).isInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember::class.java)
                assertThat(grl.omsorgsyter).isEqualTo(mor)
                assertThat(grl.omsorgsmottaker).isEqualTo(jente)
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
                    assertThat(it.omsorgsyter).isEqualTo(mor.fnr)
                    assertThat(it.omsorgsytersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                    assertThat(it.omsorgsmottaker).isEqualTo(jente.fnr)
                    assertThat(it.omsorgsmottakersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                }
                grl.forAldersvurderingOmsorgsyter().also {
                    assertThat(it.omsorgsAr).isEqualTo(2020)
                    assertThat(it.person.fnr).isEqualTo(mor.fnr)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(1980, Month.JANUARY, 1))
                    assertThat(it.alder).isEqualTo(40)
                }
                grl.forAldersvurderingOmsorgsmottaker().also {
                    assertThat(it.omsorgsAr).isEqualTo(2020)
                    assertThat(it.person.fnr).isEqualTo(jente.fnr)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(2020, Month.DECEMBER, 1))
                    assertThat(it.alder).isEqualTo(0)
                }
                grl.forTilstrekkeligOmsorgsarbeid().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtIOmsorgsår)
                    assertThat(it.omsorgsmåneder().alle()).isEqualTo(år(2021).alleMåneder())
                    assertThat(it.omsorgsmåneder().antall()).isEqualTo(12)
                    assertThat(it.omsorgsmåneder().omsorgstype()).isEqualTo(
                        DomainOmsorgskategori.BARNETRYGD
                    )
                }
                grl.forOmsorgsyterHarMestOmsorgAvAlleOmsorgsytere().also {
                    assertThat(it.omsorgsyter).isEqualTo(mor.fnr)
                    it.data.single { it.omsorgsyter == mor.fnr }.also {
                        assertThat(it.omsorgsyter).isEqualTo(mor.fnr)
                        assertThat(it.omsorgsmottaker).isEqualTo(jente.fnr)
                        assertThat(it.omsorgsår).isEqualTo(2020)
                        assertThat(it.omsorgsmåneder.alle()).isEqualTo(år(2021).alleMåneder())
                    }
                    it.data.single { it.omsorgsyter == far.fnr }.also {
                        assertThat(it.omsorgsyter).isEqualTo(far.fnr)
                        assertThat(it.omsorgsmottaker).isEqualTo(jente.fnr)
                        assertThat(it.omsorgsår).isEqualTo(2020)
                        assertThat(it.omsorgsmåneder.alle()).isEqualTo(år(2021).alleMåneder())
                    }
                }
                grl.forMedlemskapIFolketrygden().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtIOmsorgsår)
                    assertThat(it.ikkeMedlem).isEmpty()
                    assertThat(it.pliktigEllerFrivillig).isEmpty()
                    assertThat(it.omsorgsmåneder().alle()).isEqualTo(år(2021).alleMåneder())
                    assertThat(it.omsorgsmåneder().antall()).isEqualTo(12)
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

        perBarn[gutt.fnr]!!.also {
            assertThat(it).hasSize(3)
            assertThat(it.map { it.omsorgsAr }).containsAll(setOf(2020, 2021, 2021))
            it.single { it.omsorgsAr == 2021 }.also { grl ->
                assertThat(grl).isInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java)
                assertThat(grl.omsorgsyter).isEqualTo(mor)
                assertThat(grl.omsorgsmottaker).isEqualTo(gutt)
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
                    assertThat(it.omsorgsyter).isEqualTo(mor.fnr)
                    assertThat(it.omsorgsytersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                    assertThat(it.omsorgsmottaker).isEqualTo(gutt.fnr)
                    assertThat(it.omsorgsmottakersFamilierelasjoner).isEqualTo(Familierelasjoner(emptyList()))
                }
                grl.forAldersvurderingOmsorgsyter().also {
                    assertThat(it.omsorgsAr).isEqualTo(2021)
                    assertThat(it.person.fnr).isEqualTo(mor.fnr)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(1980, Month.JANUARY, 1))
                    assertThat(it.alder).isEqualTo(41)
                }
                grl.forAldersvurderingOmsorgsmottaker().also {
                    assertThat(it.omsorgsAr).isEqualTo(2021)
                    assertThat(it.person.fnr).isEqualTo(gutt.fnr)
                    assertThat(it.person.fødselsdato).isEqualTo(LocalDate.of(2018, Month.AUGUST, 7))
                    assertThat(it.alder).isEqualTo(3)
                }
                grl.forTilstrekkeligOmsorgsarbeid().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtUtenforOmsorgsår)
                    assertThat(it.omsorgsmåneder().alle()).isEqualTo(
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
                    assertThat(it.omsorgsmåneder().antall()).isEqualTo(7)
                    assertThat(it.omsorgsmåneder().omsorgstype()).isEqualTo(
                        DomainOmsorgskategori.BARNETRYGD
                    )
                }
                grl.forOmsorgsyterHarMestOmsorgAvAlleOmsorgsytere().also {
                    assertThat(it.omsorgsyter).isEqualTo(mor.fnr)
                    it.data.single { it.omsorgsyter == mor.fnr }.also {
                        assertThat(it.omsorgsyter).isEqualTo(mor.fnr)
                        assertThat(it.omsorgsmottaker).isEqualTo(gutt.fnr)
                        assertThat(it.omsorgsår).isEqualTo(2021)
                        assertThat(it.omsorgsmåneder.alle()).isEqualTo(
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
                    it.data.single { it.omsorgsyter == far.fnr }.also {
                        assertThat(it.omsorgsyter).isEqualTo(far.fnr)
                        assertThat(it.omsorgsmottaker).isEqualTo(gutt.fnr)
                        assertThat(it.omsorgsår).isEqualTo(2021)
                        assertThat(it.omsorgsmåneder.alle()).isEqualTo(år(2021).alleMåneder())
                    }
                }
                grl.forMedlemskapIFolketrygden().also {
                    assertThat(it.antallMånederRegel).isEqualTo(AntallMånederRegel.FødtUtenforOmsorgsår)
                    assertThat(it.ikkeMedlem).isEmpty()
                    assertThat(it.pliktigEllerFrivillig).isEmpty()
                    assertThat(it.omsorgsmåneder().alle()).isEqualTo(
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
                    assertThat(it.omsorgsmåneder().antall()).isEqualTo(7)
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

    @Test
    fun `test håndtering av omsorgsmåneder for kombinasjon av barnetrygd og hjelpestønad`() {
        service.lagOmsorgsopptjeningsgrunnlag(
            PersongrunnlagMelding.Mottatt(
                id = UUID.randomUUID(),
                opprettet = Instant.now(),
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = mor.fnr,
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = mor.fnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2021),
                                    tom = mars(2021),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 1234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = april(2021),
                                    tom = januar(2022),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 2345,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = februar(2022),
                                    tom = juni(2022),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 1111,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = mai(2021),
                                    tom = februar(2022),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.INFOTRYGD
                                ),
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = mars(2022),
                                    tom = desember(2022),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.INFOTRYGD
                                )
                            ),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = far.fnr,
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = februar(2022),
                                    tom = desember(2022),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 1111,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = mai(2021),
                                    tom = februar(2022),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.INFOTRYGD
                                ),
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = mars(2022),
                                    tom = desember(2022),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_4,
                                    omsorgsmottaker = ungdom.fnr,
                                    kilde = Kilde.INFOTRYGD
                                )
                            ),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            )
        ).also {
            assertThat(it).hasSize(2)

            it.single { it.omsorgsAr == 2021 }.also {
                it.omsorgsmånederForOmsorgsmottakerPerOmsorgsyter().also {
                    assertThat(it).hasSize(2)
                    assertThat(it[mor]!!.alle()).isEqualTo(Periode(mai(2021), desember(2021)).alleMåneder())
                    assertThat(it[mor]!!.antallFull()).isEqualTo(8)
                    assertThat(it[mor]!!.antallDelt()).isEqualTo(0)
                    assertThat(it[mor]!!.antall()).isEqualTo(8)
                    assertThat(it[mor]!!.kvalifisererForAutomatiskBehandling().antall()).isEqualTo(8)
                    assertThat(it[mor]!!.kvalifisererForManuellBehandling().antall()).isEqualTo(8)
                    assertThat(it[mor]!!.omsorgstype()).isEqualTo(DomainOmsorgskategori.HJELPESTØNAD)

                    assertThat(it[far]!!.alle()).isEqualTo(emptySet<YearMonth>())
                }
            }

            it.single { it.omsorgsAr == 2022 }.also {
                it.omsorgsmånederForOmsorgsmottakerPerOmsorgsyter().also {
                    assertThat(it).hasSize(2)
                    assertThat(it[mor]!!.alle()).isEqualTo(Periode(januar(2022), juni(2022)).alleMåneder())
                    assertThat(it[mor]!!.antallFull()).isEqualTo(1)
                    assertThat(it[mor]!!.antallDelt()).isEqualTo(5)
                    assertThat(it[mor]!!.antall()).isEqualTo(6)
                    assertThat(it[mor]!!.kvalifisererForAutomatiskBehandling().antall()).isEqualTo(1)
                    assertThat(it[mor]!!.kvalifisererForManuellBehandling().antall()).isEqualTo(6)
                    assertThat(it[mor]!!.omsorgstype()).isEqualTo(DomainOmsorgskategori.HJELPESTØNAD)

                    assertThat(it[far]!!.alle()).isEqualTo(Periode(februar(2022), desember(2022)).alleMåneder())
                    assertThat(it[far]!!.antallFull()).isEqualTo(0)
                    assertThat(it[far]!!.antallDelt()).isEqualTo(11)
                    assertThat(it[far]!!.antall()).isEqualTo(11)
                    assertThat(it[far]!!.kvalifisererForAutomatiskBehandling().antall()).isEqualTo(0)
                    assertThat(it[far]!!.kvalifisererForManuellBehandling().antall()).isEqualTo(11)
                    assertThat(it[far]!!.omsorgstype()).isEqualTo(DomainOmsorgskategori.HJELPESTØNAD)
                }
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
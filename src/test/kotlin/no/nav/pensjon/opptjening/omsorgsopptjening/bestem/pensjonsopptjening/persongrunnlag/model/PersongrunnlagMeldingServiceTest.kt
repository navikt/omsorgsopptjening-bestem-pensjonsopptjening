package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErForelderTilMottakerAvHjelpestønad
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErMedlemAvFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterMottarBarnetrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erEnesteAvslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertFalse
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class PersongrunnlagMeldingServiceTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
        const val OPPTJENINGSÅR = 2020
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        given(gyldigOpptjeningår.get()).willReturn(listOf(OPPTJENINGSÅR))
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i året etter opptjeningsåret (uavhengig av antall måneder)`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.FødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(2021).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret gis det ingen opptjening dersom omsorgsyter ikke har ytt omsorg i året etter omsorgsåret`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2022, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )
        assertTrue(handler.process().isEmpty())
    }

    @Test
    fun `gitt et barn født i andre måneder enn desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i opptjeningsåret (uavhengig av antall måneder) - 3 måneder`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.OCTOBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(
                                YearMonth.of(2020, Month.OCTOBER),
                                YearMonth.of(2020, Month.DECEMBER)
                            ).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født i andre måneder enn desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i opptjeningsåret (uavhengig av antall måneder) - 7 måneder`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.MAY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.FødtIOmsorgsår.IkkeFødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(
                                YearMonth.of(2020, Month.MAY),
                                YearMonth.of(2020, Month.DECEMBER)
                            ).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt et halvt år eller mer omsorg`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.SEPTEMBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(2020).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det avslås opptjening dersom omsorgsyter har ytt mindre enn et halvt år omsorg - full omsorg`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(YearMonth.of(2020, Month.JANUARY), YearMonth.of(2020, Month.MAY)).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker
                        )
                    }
                }
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>())
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det avslås opptjening dersom omsorgsyter har ytt mindre enn et halvt år omsorg - delt omsorg`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(OmsorgsopptjeningGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(YearMonth.of(2020, Month.JANUARY), YearMonth.of(2020, Month.MAY)).alleMåneder(),
                            it.omsorgsytersOmsorgsmånederForOmsorgsmottaker
                        )
                    }
                }
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>())
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest måneder omsorg i opptjeningsåret - innvilget for omsorgsyter under behandling`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.AUGUST),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.OCTOBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.NOVEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertTrue(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertFalse(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "12345678910" to 8,
                        "04010012797" to 7,
                        "01019212345" to 2,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                )
            }
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest måneder omsorg i opptjeningsåret - avslag for omsorgsyter under behandling`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.OCTOBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.AUGUST),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.NOVEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertFalse(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "04010012797" to 8,
                        "12345678910" to 7,
                        "01019212345" to 2,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                )
                assertTrue(it.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            }
        }
    }

    @Test
    fun `gitt at en omsorgyter har ytt omsorg for flere barn i det samme året, innvilges opptjening for det eldste barnet hvor alle kriterer er oppfylt`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JANUARY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().also { behandlinger ->
            assertEquals(3, behandlinger.count())
            behandlinger[0].also {
                it.assertInnvilget(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "07081812345"
                )
            }
            behandlinger[1].also {
                it.assertAvslag(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "01052012345"
                )
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
            }
            behandlinger[2].also {
                it.assertAvslag(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "01122012345"
                )
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
            }
        }
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret og flere omsorgsyter har ytt omsorg for samme barn avslås opptjening dersom en annen omsorgsyter har flere måneder med omsorg i året etter opptjeningsåret`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.FEBRUARY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.FEBRUARY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.MARCH),
                                    tom = YearMonth.of(2021, Month.MAY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertFalse(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "04010012797" to 5,
                        "12345678910" to 2,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                )
            }
        }
    }

    @Test
    fun `gitt at en omsorgsyter har fått innvilget opptjening for en omsorgsmottaker, kan ikke andre omsorgstyere innvilges for den samme omsrogsmottakeren det samme året`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().assertInnvilget(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345"
        )

        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "04010012797",
                omsorgsmottaker = "07081812345",
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>())
        }
    }

    @Test
    fun `gitt at en omsorgsyter er innvilget opptjening for en omsorgsmottaker, kan ikke den samme omsorgsyteren innvilges opptjening for andre omsorgsmottakere samme år`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().also { behandlinger ->
            assertEquals(2, behandlinger.count())
            behandlinger.first().assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandlinger.last().assertAvslag(
                "12345678910",
                "01052012345"
            )
            assertTrue(behandlinger.last().vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
        }
    }

    @Test
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal opptjening avslås - omsorgsmottaker født uten for opptjeningsår`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JULY),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JULY),
                                    omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().let { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                kotlin.test.assertTrue(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "12345678910" to 7,
                        "04010012797" to 7,
                        "01019212345" to 7,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                )
            }
            assertNull(//forventer ikke oppgave siden omsorgstyer ikke mottok i desember
                behandling.opprettOppgave(
                    oppgaveEksistererForOmsorgsyter = { _: String, _: Int -> false },
                    oppgaveEksistererForOmsorgsmottaker = { _: String, _: Int -> false }
                )
            )
        }
    }

    @Test
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal opptjening avslås - omsorgsmottager født i desember i opptjeningsåret`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JULY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().let { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertTrue(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "12345678910" to 6,
                        "04010012797" to 6,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                )
            }
            assertInstanceOf(//forventer oppgave siden omsorgsyter mokkok i desember
                Oppgave::class.java, behandling.opprettOppgave(
                    oppgaveEksistererForOmsorgsyter = { _: String, _: Int -> false },
                    oppgaveEksistererForOmsorgsmottaker = { _: String, _: Int -> false }
                )).also {
                assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår::class.java, it.detaljer)
            }
        }
    }

    @Test
    fun `gitt en omsorgsmottaker som har blitt innvilget for en omsorgsyter i et år, kan innvilges for en annen omsorgsyter et annet år`() {
        given(gyldigOpptjeningår.get()).willReturn(listOf(2020, 2021))

        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().single().assertInnvilget(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            år = OPPTJENINGSÅR,
        )

        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "04010012797",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().assertInnvilget(
            omsorgsyter = "04010012797",
            omsorgsmottaker = "07081812345",
            år = 2021,
        )
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker over 6 år med hjelpestønad skal innvilges omsorgsopptjening dersom barnetrygd og hjelpestønad har et halvt år eller mer overlappende måneder`() {
        wiremock.givenThat(
            get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", equalTo("12345678910")) //mor
                .withQueryParam("fomAr", equalTo("2019"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":2019,
                                        "poeng":1.5,
                                        "pensjonspoengType":"OBO6H"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        wiremock.givenThat(
            get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", equalTo("12345678910")) //mor
                .withQueryParam("fomAr", equalTo("2020"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":2020,
                                        "poeng":1.5,
                                        "pensjonspoengType":"OBO6H"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        wiremock.givenThat(
            get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", equalTo("04010012797")) //far
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng":null
                            }
                        """.trimIndent()
                        )
                )
        )

        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker over 6 år med hjelpestønad skal avslås omsorgsyter og omsorgsmottaker ikke har en foreldre-barn relasjon`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "01019212345",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "01019212345",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "01019212345",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
            ).also {
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsyterErForelderTilMottakerAvHjelpestønad.Vurdering>())
            }
        }
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker med hjelpestønad skal avslås dersom barnetrygd og hjelpestønad har et halvt år eller mindre overlappende måneder`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2020, Month.MAY),
                                    tom = YearMonth.of(2020, Month.JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som ikke mottar barnetrygd for en omsorgsmottaker med hjelpestønad skal avslås`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "03041212345",
                omsorgstype = DomainOmsorgstype.HJELPESTØNAD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som mottar barnetrygd for en omsorgsmottaker under 6 år med hjelpestønad skal innvilges omsorgsopptjening for barnetrygd`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.INFOTRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
            )
        }
    }

    @Test
    fun `en omsorgsyter som ikke er medlem i folketrygden skal avslås`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Nei,
                                    utbetalt = 7234
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterErMedlemAvFolketrygden.Vurdering>())
        }
    }

    @Test
    fun `en omsorgsyter som ikke får utbetalt barnetryd skal avslås`() {
        repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01052012345",
                                    kilde = Kilde.BARNETRYGD,
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                    utbetalt = 0
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345",
                omsorgstype = DomainOmsorgstype.BARNETRYGD,
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterMottarBarnetrgyd.Vurdering>())
        }
    }

    private fun FullførtBehandling.assertInnvilget(
        omsorgsyter: String,
        omsorgsmottaker: String,
        år: Int = OPPTJENINGSÅR,
        omsorgstype: DomainOmsorgstype = DomainOmsorgstype.BARNETRYGD,
    ): FullførtBehandling {
        assertEquals(år, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(omsorgstype, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Innvilget::class.java, this.utfall)
        return this
    }

    private fun FullførtBehandling.assertAvslag(
        omsorgsyter: String,
        omsorgsmottaker: String,
        omsorgstype: DomainOmsorgstype = DomainOmsorgstype.BARNETRYGD,
    ): FullførtBehandling {
        assertEquals(OPPTJENINGSÅR, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(omsorgstype, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Avslag::class.java, this.utfall)
        return this
    }
}
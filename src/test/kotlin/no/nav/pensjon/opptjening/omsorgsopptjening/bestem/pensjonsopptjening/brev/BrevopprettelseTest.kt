package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BrevÅrsak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

internal class BrevopprettelseTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingService: PersongrunnlagMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var brevRepository: BrevRepository

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
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(OPPTJENINGSÅR)
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal sende brev dersom omsorgsyter ikke mottok omsorgspoeng i året før omsorgsåret`() {
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(equalToJson("""
                    {
                        "fnr" : "04010012797"
                    }
                """.trimIndent(), false, true))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":$OPPTJENINGSÅR,
                                        "poeng":7.4,
                                        "pensjonspoengType":"PPI"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        persongrunnlagRepo.lagre(
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
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingService.process()!!.first().single().also { behandling ->
            assertTrue(behandling.erInnvilget())

            assertInstanceOf(Brev::class.java, brevRepository.findForBehandling(behandling.id).singleOrNull()).also {
                assertThat(it.årsak).isEqualTo(BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR)
            }
        }
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal sende brev dersom omsorgsyters omsorgspoeng som godskrives er høyere enn annen forelders pensjonspoeng for inntekt i omsorgsåret`() {
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(equalToJson("""
                    {
                       "fnr": "12345678910",
                       "fomAr": 2019
                    }
                """.trimIndent(), false, true))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":${OPPTJENINGSÅR - 1},
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
            WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(equalToJson("""
                    {
                        "fnr" : "04010012797"
                    }
                """.trimIndent(), false, true))
                .willReturn(
                    WireMock.aResponse()
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

        persongrunnlagRepo.lagre(
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
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingService.process()!!.first().single().also { behandling ->
            assertTrue(behandling.erInnvilget())

            assertInstanceOf(Brev::class.java, brevRepository.findForBehandling(behandling.id).singleOrNull()).also {
                assertThat(it.årsak).isEqualTo(BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG)
            }
        }
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal ikke sende brev dersom omsorgsyters omsorgspoeng som godskrives er lavere enn annen forelders pensjonspoeng for inntekt i omsorgsåret`() {
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(equalToJson("""
                    {
                        "fnr" : "12345678910",
                        "fomAr": 2019
                    }
                """.trimIndent(), false, true))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":${OPPTJENINGSÅR - 1},
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
            WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(equalToJson("""
                    {
                        "fnr" : "04010012797"
                    }
                """.trimIndent(), false, true))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":${OPPTJENINGSÅR},
                                        "poeng":8,
                                        "pensjonspoengType":"PPI"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        persongrunnlagRepo.lagre(
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
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingService.process()!!.first().single().also { behandling ->
            assertTrue(behandling.erInnvilget())
            assertTrue(brevRepository.findForBehandling(behandling.id).isEmpty())
        }
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal bare sende 1 brev`() {
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
                .withRequestBody(equalToJson("""
                    {
                        "fnr" : "04010012797"
                    }
                """.trimIndent(), false, true))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":$OPPTJENINGSÅR,
                                        "poeng":7.4,
                                        "pensjonspoengType":"PPI"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        persongrunnlagRepo.lagre(
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
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 3123,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        persongrunnlagMeldingService.process()!!.first().also { behandling ->
            behandling.alle().let { fullførteBehandlinger ->
                fullførteBehandlinger[0].let {
                    assertThat(it.erInnvilget()).isTrue()
                    assertThat(it.omsorgsmottaker).isEqualTo("03041212345")
                    assertThat(it.omsorgstype.toString()).isEqualTo("HJELPESTØNAD")
                    assertInstanceOf(Brev::class.java, brevRepository.findForBehandling(it.id).single()).also { brev ->
                        assertThat(brev.årsak).isEqualTo(BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR)
                    }
                }
                fullførteBehandlinger[1].let {
                    assertThat(it.erAvslag()).isTrue()
                    assertThat(it.omsorgsmottaker).isEqualTo("01122012345")
                    assertThat(it.omsorgstype.toString()).isEqualTo("BARNETRYGD")
                    assertThat(brevRepository.findForBehandling(it.id)).isEmpty()
                }
            }
        }
    }
}
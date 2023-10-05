package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth

internal class BrevopprettelseTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var omsorgsarbeidRepo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var omsorgsarbeidMeldingService: OmsorgsarbeidMeldingService

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
        BDDMockito.given(gyldigOpptjeningår.get()).willReturn(listOf(OPPTJENINGSÅR))
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal sende brev dersom omsorgsyter ikke mottok omsorgspoeng i året før omsorgsåret`() {
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.givenThat(
            WireMock.get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", WireMock.equalTo("04010012797")) //far
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

        omsorgsarbeidRepo.persist(
            OmsorgsarbeidMelding.Lest(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
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

        omsorgsarbeidMeldingService.process().single().also { behandling ->
            assertTrue(behandling.erInnvilget())

            assertInstanceOf(Brev::class.java, brevRepository.findForBehandling(behandling.id).singleOrNull())
        }
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal sende brev dersom omsorgsyters omsorgspoeng som godskrives er høyere enn annen forelders pensjonspoeng for inntekt i omsorgsåret`() {
        wiremock.givenThat(
            WireMock.get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", WireMock.equalTo("12345678910")) //mor
                .withQueryParam("fomAr", WireMock.equalTo("2019"))
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
            WireMock.get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", WireMock.equalTo("04010012797")) //far
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

        omsorgsarbeidRepo.persist(
            OmsorgsarbeidMelding.Lest(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
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

        omsorgsarbeidMeldingService.process().single().also { behandling ->
            assertTrue(behandling.erInnvilget())

            assertInstanceOf(Brev::class.java, brevRepository.findForBehandling(behandling.id).singleOrNull())
        }
    }

    @Test
    fun `innvilget omsorgsopptjening for barn over 6 år skal ikke sende brev dersom omsorgsyters omsorgspoeng som godskrives er lavere enn annen forelders pensjonspoeng for inntekt i omsorgsåret`() {
        wiremock.givenThat(
            WireMock.get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", WireMock.equalTo("12345678910")) //mor
                .withQueryParam("fomAr", WireMock.equalTo("2019"))
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
            WireMock.get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", WireMock.equalTo("04010012797")) //far
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

        omsorgsarbeidRepo.persist(
            OmsorgsarbeidMelding.Lest(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
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

        omsorgsarbeidMeldingService.process().single().also { behandling ->
            assertTrue(behandling.erInnvilget())

            assertTrue(brevRepository.findForBehandling(behandling.id).isEmpty())
        }
    }
}
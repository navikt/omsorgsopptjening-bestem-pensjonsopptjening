package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.test.assertEquals

class HentPensjonspoengClientTest : SpringContextTest.NoKafka() {

    @Autowired
    @Qualifier("hentPensjonspoeng")
    private lateinit var hentPensjonspoengClient: HentPensjonspoengClient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `happy path`() {
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innsendingId ->
                wiremock.givenThat(
                    get(urlPathEqualTo(POPP_PENSJONSPOENG_PATH)).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """
                        {
                            "pensjonspoeng": [
                                {
                                    "pensjonspoengId":"abc123",
                                    "ar":2020,
                                    "poeng":1.5,
                                    "pensjonspoengType":"OBU6",
                                    "maxUforegrad":null
                                },
                                {
                                    "pensjonspoengId":"abc123",
                                    "ar":2021,
                                    "poeng":2.8,
                                    "pensjonspoengType":"OBU6",
                                    "maxUforegrad":null
                                },
                                {
                                    "pensjonspoengId":"abc123",
                                    "ar":2022,
                                    "poeng":3.7,
                                    "pensjonspoengType":"OBO6H",
                                    "maxUforegrad":null
                                },
                                {
                                    "pensjonspoengId":"abc123",
                                    "ar":2023,
                                    "poeng":3.7,
                                    "pensjonspoengType":"PPI",
                                    "maxUforegrad":null
                                }
                            ]
                        }
                    """.trimIndent()
                            )
                    )
                )

                assertEquals(
                    Pensjonspoeng.Omsorg(
                        år = 2020,
                        poeng = 1.5,
                        type = DomainOmsorgstype.BARNETRYGD
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForOmsorgsarbeid(
                        fnr = "12345",
                        år = 2020,
                        type = DomainOmsorgstype.BARNETRYGD
                    )
                )

                assertEquals(
                    Pensjonspoeng.Omsorg(
                        år = 2021,
                        poeng = 2.8,
                        type = DomainOmsorgstype.BARNETRYGD
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForOmsorgsarbeid(
                        fnr = "12345",
                        år = 2021,
                        type = DomainOmsorgstype.BARNETRYGD
                    )
                )

                assertEquals(
                    Pensjonspoeng.Omsorg(
                        år = 2022,
                        poeng = 3.7,
                        type = DomainOmsorgstype.HJELPESTØNAD
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForOmsorgsarbeid(
                        fnr = "12345",
                        år = 2022,
                        type = DomainOmsorgstype.HJELPESTØNAD
                    )
                )

                assertEquals(
                    Pensjonspoeng.Inntekt(
                        år = 2023,
                        poeng = 3.7,
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForInntekt(
                        fnr = "12345",
                        år = 2023,
                    )
                )

                wiremock.verify(
                    getRequestedFor(urlEqualTo("/api/pensjonspoeng?fomAr=2022&tomAr=2022&pensjonspoengType=OBO6H"))
                        .withHeader("fnr", equalTo("12345"))
                        .withHeader("Nav-Call-Id", equalTo(correlationId.toString()))
                        .withHeader("Nav-Consumer-Id", equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innsendingId.toString()))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer " + TokenProviderConfig.MOCK_TOKEN))
                )

                wiremock.verify(
                    getRequestedFor(urlEqualTo("/api/pensjonspoeng?fomAr=2023&tomAr=2023&pensjonspoengType=PPI"))
                        .withHeader("fnr", equalTo("12345"))
                        .withHeader("Nav-Call-Id", equalTo(correlationId.toString()))
                        .withHeader("Nav-Consumer-Id", equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innsendingId.toString()))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer " + TokenProviderConfig.MOCK_TOKEN))
                )
            }
        }
    }

    @Test
    fun `returnerer pensjonspoeng lik 0 for forespurt år og type dersom response er null`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.givenThat(
                    get(urlPathEqualTo(POPP_PENSJONSPOENG_PATH)).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """
                        {
                            "pensjonspoeng": null
                        }
                    """.trimIndent()
                            )
                    )
                )

                assertEquals(
                    Pensjonspoeng.Omsorg(
                        år = 2020,
                        poeng = 0.0,
                        type = DomainOmsorgstype.BARNETRYGD
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForOmsorgsarbeid(
                        fnr = "12345",
                        år = 2020,
                        type = DomainOmsorgstype.BARNETRYGD
                    )
                )

                assertEquals(
                    Pensjonspoeng.Inntekt(
                        år = 2020,
                        poeng = 0.0,
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForInntekt(
                        fnr = "12345",
                        år = 2020,
                    )
                )
            }
        }
    }

    @Test
    fun `returnerer pensjonspoeng lik 0 for forespurt år og type dersom response er tom liste`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.ingenPensjonspoeng("12345")

                assertEquals(
                    Pensjonspoeng.Omsorg(
                        år = 2020,
                        poeng = 0.0,
                        type = DomainOmsorgstype.BARNETRYGD
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForOmsorgsarbeid(
                        fnr = "12345",
                        år = 2020,
                        type = DomainOmsorgstype.BARNETRYGD
                    )
                )

                assertEquals(
                    Pensjonspoeng.Inntekt(
                        år = 2020,
                        poeng = 0.0,
                    ),
                    hentPensjonspoengClient.hentPensjonspoengForInntekt(
                        fnr = "12345",
                        år = 2020,
                    )
                )
            }
        }
    }

    @Test
    fun `kaster exception dersom response inneholder data, men ikke nøyaktig ett element for forespurt år og type`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.givenThat(
                    get(urlPathEqualTo(POPP_PENSJONSPOENG_PATH)).willReturn(
                        aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """
                        {
                            "pensjonspoeng": [
                                {
                                    "pensjonspoengId":"abc123",
                                    "ar":2020,
                                    "poeng":1.5,
                                    "pensjonspoengType":"OBU6",
                                    "maxUforegrad":null
                                },
                                {
                                    "pensjonspoengId":"abc123",
                                    "ar":2020,
                                    "poeng":2.8,
                                    "pensjonspoengType":"OBU6",
                                    "maxUforegrad":null
                                }
                            ]
                        }
                    """.trimIndent()
                            )
                    )
                )

                assertThrows<HentPensjonspoengClientException> {
                    hentPensjonspoengClient.hentPensjonspoengForOmsorgsarbeid(
                        fnr = "12345",
                        år = 2020,
                        type = DomainOmsorgstype.HJELPESTØNAD
                    )
                }
            }
        }
    }
}
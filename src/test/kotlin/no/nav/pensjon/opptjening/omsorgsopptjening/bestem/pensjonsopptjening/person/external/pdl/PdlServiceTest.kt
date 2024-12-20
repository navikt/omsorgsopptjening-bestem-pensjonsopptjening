package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.PDL_PATH
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.WIREMOCK_PORT
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig.Companion.MOCK_TOKEN
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslagException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.web.client.RestClientException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class PdlServiceTest {

    private val UUID_REGEX =
        "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$"

    companion object {
        const val FNR = "11111111111"

        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    private val pdlService: PdlService = PdlService(
        PdlClient(
            pdlUrl = "${wiremock.baseUrl()}$PDL_PATH",
            tokenProvider = mock { on { getToken() }.thenReturn(MOCK_TOKEN) },
            metrics = mock(),
            graphqlQuery = GraphqlQuery(
                hentPersonQuery = ClassPathResource("pdl/folkeregisteridentifikator.graphql"),
                hentAktorIdQuery = ClassPathResource("pdl/hentAktorId.graphql")
            ),
            restTemplate = RestTemplateBuilder().build()
        )
    )


    @Test
    fun `Given hentPerson then call pdl one time`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_1bruk.json")
                    )
                )

                pdlService.hentPerson(FNR)

                wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }
    }

    @Test
    fun `Given hentPerson Then call pdl with fnr`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_1bruk.json")
                    )
                )

                pdlService.hentPerson(FNR)

                wiremock.verify(
                    WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)).withRequestBody(containing(FNR))
                )
            }
        }
    }

    @Test
    fun `Given hentPerson Then call pdl with token and other headers`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_1bruk.json")
                    )
                )

                pdlService.hentPerson(FNR)

                wiremock.verify(
                    WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH))
                        .withHeader("Authorization", WireMock.equalTo("Bearer $MOCK_TOKEN"))
                        .withHeader("Accept", WireMock.equalTo("application/json"))
                        .withHeader("Content-Type", WireMock.equalTo("application/json"))
                        .withHeader("Nav-Consumer-Id", WireMock.equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                        .withHeader("Tema", WireMock.equalTo("PEN"))
                        .withHeader(
                            "x-correlation-id",
                            WireMock.matching(UUID_REGEX)
                        )
                        .withHeader(
                            "Nav-Call-Id",
                            WireMock.matching(UUID_REGEX)
                        )
                )
            }
        }
    }

    @Test
    fun `Given PDL return folkeregisteridentifikator When hentPerson Then return pdl response`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("fnr_1bruk.json")
                    )
                )

                assertNotNull(pdlService.hentPerson(FNR))
            }
        }
    }

    @Test
    @Disabled("Treig som følge av backoff ved retry")
    fun `Given other code than 200 When getting person Then retry 3 times before give up`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(WireMock.aResponse().withStatus(401))
                )
                assertThrows<RestClientException> { pdlService.hentPerson(FNR) }
                wiremock.verify(4, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }
    }

    @Test
    @Disabled("Treig som følge av backoff ved retry")
    fun `Given server error When getting person Then retry 3 times before give up`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("error_server_error.json")
                    )
                )

                val error = assertThrows<PdlException> { pdlService.hentPerson(FNR) }
                assertEquals(PdlErrorCode.SERVER_ERROR, error.code)
                wiremock.verify(4, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }

    }

    @Test
    fun `Given not found When calling pdl Then throw PdlException`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("error_not_found.json")
                    )
                )

                val error = assertThrows<PersonOppslagException> { pdlService.hentPerson(FNR) }
                assertInstanceOf(PdlException::class.java, error.cause).also {
                    assertEquals(PdlErrorCode.NOT_FOUND, it.code)
                }
                wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }
    }

    @Test
    fun `Given unauthenticated When calling pdl Then throw PdlException`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("error_unauthenticated.json")
                    )
                )

                val error = assertThrows<PersonOppslagException> { pdlService.hentPerson(FNR) }
                assertInstanceOf(PdlException::class.java, error.cause).also {
                    assertEquals(PdlErrorCode.UNAUTHENTICATED, it.code)
                }
                wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }
    }

    @Test
    fun `Given unauthorized When calling pdl Then throw PdlException`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("error_unauthorized.json")
                    )
                )

                val error = assertThrows<PersonOppslagException> { pdlService.hentPerson(FNR) }
                assertInstanceOf(PdlException::class.java, error.cause).also {
                    assertEquals(PdlErrorCode.UNAUTHORIZED, it.code)
                }
                wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }
    }

    @Test
    fun `Given bad request When calling pdl Then throw PdlException`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBodyFile("error_bad_request.json")
                    )
                )

                val error = assertThrows<PersonOppslagException> { pdlService.hentPerson(FNR) }
                assertInstanceOf(PdlException::class.java, error.cause).also {
                    assertEquals(PdlErrorCode.BAD_REQUEST, it.code)
                }
                wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
            }
        }
    }
}
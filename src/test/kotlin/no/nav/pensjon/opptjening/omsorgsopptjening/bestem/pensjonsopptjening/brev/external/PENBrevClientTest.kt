package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.badRequest
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.EksternReferanseId
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.WIREMOCK_PORT
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.springframework.http.HttpHeaders
import java.net.URI
import java.time.Year
import kotlin.test.Test

internal class PENBrevClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    private val client: PENBrevClient = PENBrevClient(
        baseUrl = wiremock.baseUrl(),
        tokenProvider = mock { on { getToken() }.thenReturn(TokenProviderConfig.MOCK_TOKEN) },
        penBrevMetricsMåling = mock()
    )

    @Test
    fun testSerializeRegular() {
        val request = PENBrevClient.SendBrevRequest(omsorgsår = Year.of(2010), eksternReferanseId = "42")
        val json = serialize(request)
        val mapper = ObjectMapper()
        val jsonTree = mapper.readTree(json)
        val expectedTree =
            mapper.readTree("""{"brevdata":{"aarInnvilgetOmsorgspoeng":2010},"eksternReferanseId":"42"}""")
        assertThat(jsonTree).isEqualTo(expectedTree)
    }


    @Test
    fun `kaster exception dersom kall ikke gikk bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                val path = URI(PENBrevClient.createPath(wiremock.baseUrl(), "42")).toURL().path
                wiremock.givenThat(
                    post(urlPathEqualTo(path))
                        .willReturn(serverError())
                )

                assertThrows<BrevClientException> {
                    client.sendBrev(
                        "42",
                        EksternReferanseId("referanse"),
                        Year.of(2020),
                    )
                }
            }
        }
    }

    @Test
    fun `returnerer id for opprettet brev hvis kall går bra`() {
        val sakId = "42"
        val path = URI(PENBrevClient.createPath(wiremock.baseUrl(), sakId)).toURL().path
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    post(urlPathEqualTo(path))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
                        .withRequestBody(
                            equalToJson(
                                """{"brevdata":{"aarInnvilgetOmsorgspoeng":2020},
                                    "eksternReferanseId":"${'$'}{json-unit.any-string}"
                                   }""".trimMargin()
                            )
                        )
                        .willReturn(
                            ok()
                                .withBody("""{"journalpostId":"123", "error":null}""")
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )
                )

                assertThat(client.sendBrev("42", EksternReferanseId("referanse"), Year.of(2020)))
                    .isEqualTo(Journalpost("123"))
            }
        }
    }

    @Test
    fun `returnerer ikke funnet hvis vedtak ikke finnes`() {
        val sakId = "42"
        val path = URI(PENBrevClient.createPath(wiremock.baseUrl(), sakId)).toURL().path
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    post(urlPathEqualTo(path))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
                        .withRequestBody(
                            equalToJson(
                                """{"brevdata":{"aarInnvilgetOmsorgspoeng":2020},
                                    "eksternReferanseId":"${'$'}{json-unit.any-string}"
                                   }""".trimMargin()
                            )
                        )
                        .willReturn(
                            notFound()
                        )
                )
                assertThatThrownBy {
                    (client.sendBrev("42", EksternReferanseId("referanse"), Year.of(2020)))
                }
                    .isInstanceOf(BrevClientException::class.java)
                    .hasMessage("Feil fra brevtjenesten: vedtak finnes ikke")
            }
        }
    }

    @Test
    fun `returnerer feilårsak dersom opprettelse av brev feilet`() {
        val sakId = "42"
        val path = URI(PENBrevClient.createPath(wiremock.baseUrl(), sakId)).toURL().path
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    post(urlPathEqualTo(path))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
                        .withRequestBody(
                            equalToJson(
                                """{"brevdata":{"aarInnvilgetOmsorgspoeng":2020},
                                    "eksternReferanseId":"${'$'}{json-unit.any-string}"
                                   }""".trimMargin()
                            )
                        )
                        .willReturn(
                            badRequest()
                                .withBody("""{ "error": { "tekniskgrunn": "noe mangler", "beskrivelse": "dette kan leses"}}""")
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )
                )
                assertThatThrownBy {
                    (client.sendBrev("42", EksternReferanseId("referanse"), Year.of(2020)))
                }
                    .isInstanceOf(BrevClientException::class.java)
                    .hasMessage("Feil fra brevtjenesten: teknisk grunn: noe mangler, beskrivelse: dette kan leses")
            }
        }
    }

    @Test
    fun `returnerer feilårsak dersom opprettelse av brev lykkes men med feilmelding`() {
        val sakId = "42"
        val path = URI(PENBrevClient.createPath(wiremock.baseUrl(), sakId)).toURL().path
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    post(urlPathEqualTo(path))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
                        .withRequestBody(
                            equalToJson(
                                """{"brevdata":{"aarInnvilgetOmsorgspoeng":2020},
                                    "eksternReferanseId":"${'$'}{json-unit.any-string}"
                                   }""".trimMargin()
                            )
                        )
                        .willReturn(
                            ok()
                                .withBody("""{"journalpostId":"123", "error": { "tekniskgrunn": "noe mangler", "beskrivelse": "dette kan leses"}}""")
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )
                )
                assertThatThrownBy {
                    (client.sendBrev("42", EksternReferanseId("referanse"), Year.of(2020)))
                }
                    .isInstanceOf(BrevClientException::class.java)
                    .hasMessage("Brevtjenesten svarte ok, med journalId:123 og feil, teknisk grunn:noe mangler og beskrivelse: dette kan leses")
            }
        }
    }
}
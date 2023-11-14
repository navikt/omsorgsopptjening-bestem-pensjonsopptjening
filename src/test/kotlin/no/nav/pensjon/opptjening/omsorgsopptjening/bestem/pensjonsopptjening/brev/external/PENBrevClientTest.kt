package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.EksternReferanseId
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import java.net.URL
import java.time.Year
import kotlin.test.Test

class PENBrevClientTest(
    @Value("\${PEN_BASE_URL}")
    private val baseUrl: String,
) : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var client: PENBrevClient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @org.junit.jupiter.api.Test
    fun testSerializeRegular() {
        val request = PENBrevClient.SendBrevRequest(omsorgsår = Year.of(2010), eksternReferanseId = "42")
        val json = serialize(request)
        val mapper = ObjectMapper()
        val jsonTree = mapper.readTree(json)
        val expectedTree = mapper.readTree("""{"brevdata":{"aarInnvilgetOmsorgspoeng":2010},"eksternReferanseId":"42"}""")
        assertThat(jsonTree).isEqualTo(expectedTree)
    }


    @Test
    fun `kaster exception dersom kall ikke gikk bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                val path = URL(PENBrevClient.sendBrevUrl(baseUrl,"42")).path
                println("Wiremock-path: ${path}")
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
        val path = URL(PENBrevClient.sendBrevUrl(baseUrl, sakId)).path
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
        val path = URL(PENBrevClient.sendBrevUrl(baseUrl, sakId)).path
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
                            notFound() // TODO: noe mer her? 404 kan skyldes andre ting også
//                                .withBody("""{"journalpostId":"123"}""")
//                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
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
        val path = URL(PENBrevClient.sendBrevUrl(baseUrl, sakId)).path
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

}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.matching.UrlPattern
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Journalpost
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals

class BrevKlientTest(
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

    @Test
    fun `kaster exception dersom kall ikke gikk bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                val url = baseUrl + PENBrevClient.sendBrevPath("42")
                println("Wiremock-path: ${url}")
                wiremock.givenThat(
                    WireMock.post("*")
//                    WireMock.post(WireMock.urlPathEqualTo(url))
                        .willReturn(WireMock.serverError())
                )

                assertThrows<BrevClientException> {
                    client.sendBrev(
                        "42",
                        "12345678912",
                        Year.of(2020),
                    )
                }
            }
        }
    }

    @Test
    fun `returnerer id for opprettet brev hvis kall går bra`() {
        val sakId = "42"
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    WireMock.post(UrlPattern.ANY)
                    //                    WireMock.post(WireMock.urlPathEqualTo(PENBrevClient.sendBrevPath(sakId)))
//                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
//                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
//                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
//                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
//                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
//                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
//                        .withRequestBody(
//                            equalToJson(
//                                """{"brevdata":{"aarInvilgetOmsorgspoeng":2020},"eksternReferanseId":"${'$'}{json-unit.any-string}"}"""
//                            )
//                        )
                        .willReturn(
                            WireMock.created()
                                .withBody("""{"journalpostId":"123"}""")
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )
                )

                assertThat(client.sendBrev("42", "123451234512", Year.of(2020)))
                    .isEqualTo(Journalpost("123"))
            }
        }
    }
}
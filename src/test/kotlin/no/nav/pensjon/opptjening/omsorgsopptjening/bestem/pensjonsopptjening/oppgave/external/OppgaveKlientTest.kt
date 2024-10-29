package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.OPPGAVE_PATH
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.WIREMOCK_PORT
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals

class OppgaveKlientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    private val client: OppgaveKlient = OppgaveKlient(
        oppgaveUrl = "${wiremock.baseUrl()}/$OPPGAVE_PATH",
        tokenProvider = mock { on { getToken() }.thenReturn(TokenProviderConfig.MOCK_TOKEN) },
        restTemplate = RestTemplateBuilder().build()
    )

    @Test
    fun `kaster exception dersom kall ikke gikk bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.givenThat(
                    WireMock.post(WireMock.urlPathEqualTo(OPPGAVE_PATH))
                        .willReturn(WireMock.serverError())
                )

                assertThrows<OppgaveKlientException> {
                    client.opprettOppgave(
                        aktoerId = "interdum",
                        sakId = "habitasse",
                        beskrivelse = "ferri",
                        tildeltEnhetsnr = "ludus"
                    )
                }
            }
        }
    }

    @Test
    fun `returnerer id for opprettet oppgave hvis kall går bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    WireMock.post(WireMock.urlPathEqualTo(OPPGAVE_PATH))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
                        .withRequestBody(
                            equalToJson(
                                """
                            {
                                "aktoerId":"interdum",
                                "saksreferanse":"habitasse",
                                "beskrivelse":"ferri",
                                "tildeltEnhetsnr":"ludus",
                                "tema":"PEN",
                                "behandlingstema":"ab0341",
                                "oppgavetype":"KRA",
                                "opprettetAvEnhetsnr":"9999",
                                "aktivDato": "${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}",
                                "fristFerdigstillelse": "${
                                    LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_DATE)
                                }",
                                "prioritet":"LAV"
                            }
                        """.trimIndent()
                            )
                        )
                        .willReturn(
                            WireMock.created()
                                .withBody("""{"id":123}""")
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )
                )

                assertEquals(
                    "123", client.opprettOppgave(
                        aktoerId = "interdum",
                        sakId = "habitasse",
                        beskrivelse = "ferri",
                        tildeltEnhetsnr = "ludus"
                    )
                )
            }
        }
    }

    @Test
    fun `henter info for oppgave`() {
        val oppgaveId = 1234
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    WireMock.get(WireMock.urlPathEqualTo("$OPPGAVE_PATH/$oppgaveId"))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))

                        .willReturn(
                            WireMock.ok()
                                .withBody(
                                    """
                            {
                                "id":"1234",
                                "versjon":"2",
                                "saksreferanse":"habitasse",
                                "beskrivelse":"ferri",
                                "tildeltEnhetsnr":"ludus",
                                "tema":"PEN",
                                "behandlingstema":"ab0341",
                                "oppgavetype":"KRA",
                                "opprettetAvEnhetsnr":"9999",
                                "aktivDato": "${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}",
                                "fristFerdigstillelse": "${
                                        LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_DATE)
                                    }",
                                "status":"OPPRETTET",
                                "prioritet":"LAV"
                            }
                        """.trimIndent()
                                )
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )
                )

                assertThat(client.hentOppgaveInfo("1234"))
                    .isEqualTo(
                        OppgaveInfo("1234", 2, OppgaveStatus.OPPRETTET)
                    )
            }
        }
    }

    @Test
    fun `kansellere en oppgave som finnes`() {
        val oppgaveId = 1234
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    WireMock.patch(WireMock.urlPathEqualTo("$OPPGAVE_PATH/$oppgaveId"))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
                        .withHeader("X-Correlation-ID", equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesingId.toString()))
                        .withRequestBody(
                            equalToJson(
                                """
                            {
                                "versjon":2,
                                "status":"FEILREGISTRERT"
                            }
                        """.trimIndent()
                            )
                        )
                        .willReturn(
                            WireMock.ok()
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        )

                )
                assertThat(client.kansellerOppgave("1234", 2))
                    .isEqualTo(KansellerOppgaveRespons.OPPDATERT_OK)
            }
        }
    }
}
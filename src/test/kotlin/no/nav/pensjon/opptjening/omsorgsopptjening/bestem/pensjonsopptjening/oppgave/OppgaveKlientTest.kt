package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals

class OppgaveKlientTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var client: OppgaveKlient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `kaster exception dersom kall ikke gikk bra`() {
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

    @Test
    fun `returnerer id for opprettet oppgave hvis kall går bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    WireMock.post(WireMock.urlPathEqualTo(OPPGAVE_PATH))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
                        .withHeader("Tema", equalTo("PEN"))
                        .withHeader("Nav-Call-Id", equalTo(correlationId.toString()))
                        .withHeader("Nav-Consumer-Id", equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                        .withHeader("x-correlation-id", equalTo(correlationId.toString()))
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
}
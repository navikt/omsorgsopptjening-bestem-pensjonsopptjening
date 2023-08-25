package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import kotlin.test.assertContains

class PoppClientTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var client: PoppClient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `kaster exception med informasjon dersom kall svarer med server error`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.givenThat(
                    post(urlPathEqualTo(POPP_PATH))
                        .willReturn(serverError())
                )

                assertThrows<PoppClientExecption> {
                    client.lagre(
                        omsorgsyter = "elaboraret",
                        omsorgsÅr = 6669,
                        omsorgstype = DomainOmsorgstype.BARNETRYGD,
                        kilde = DomainKilde.BARNETRYGD,
                        omsorgsmottaker = "nunc"
                    )
                }.also {
                    assertContains(it.toString(), "500")
                }
            }
        }
    }

    @Test
    fun `kaster exception med informasjon om feil dersom kall svarer med spesialkode 512`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.givenThat(
                    post(urlPathEqualTo(POPP_PATH))
                        .willReturn(
                            aResponse()
                                .withStatus(512)
                                .withBody(
                                    """
                                    {
                                        "body": {
                                            "exception":"PersonDoesNotExistExceptionDto",
                                            "message":"Personen eksisterer ikke i POPP"
                                        }
                                    }
                                """.trimIndent()
                                )
                        )
                )

                assertThrows<PoppClientExecption> {
                    client.lagre(
                        omsorgsyter = "elaboraret",
                        omsorgsÅr = 6669,
                        omsorgstype = DomainOmsorgstype.BARNETRYGD,
                        kilde = DomainKilde.BARNETRYGD,
                        omsorgsmottaker = "nunc"
                    )
                }.also {
                    assertContains(it.toString(), "PersonDoesNotExistExceptionDto")
                    assertContains(it.toString(), "Personen eksisterer ikke i POPP")
                }
            }
        }
    }


    @Test
    fun `returnerer ingenting hvis kall går bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    post(urlPathEqualTo(POPP_PATH))
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
                                    "omsorg": {
                                        "fnr":"elaboraret",
                                        "ar":6669,
                                        "omsorgType":"OBU6",
                                        "kilde":"BA",
                                        "fnrOmsorgFor":"nunc"
                                    }
                                }
                            """.trimIndent()
                            )
                        )
                        .willReturn(ok())
                )

                assertEquals(
                    Unit, client.lagre(
                        omsorgsyter = "elaboraret",
                        omsorgsÅr = 6669,
                        omsorgstype = DomainOmsorgstype.BARNETRYGD,
                        kilde = DomainKilde.BARNETRYGD,
                        omsorgsmottaker = "nunc"
                    )
                )
            }
        }
    }
}
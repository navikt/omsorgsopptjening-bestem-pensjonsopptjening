package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
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
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
                .willReturn(WireMock.serverError())
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

    @Test
    fun `kaster exception med informasjon om feil dersom kall svarer med spesialkode 512`() {
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
                .willReturn(
                    WireMock.aResponse()
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


    @Test
    fun `returnerer ingenting hvis kall går bra`() {
        Mdc.scopedMdc(CorrelationId.name, "correlationId") {
            wiremock.givenThat(
                WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
                    .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer test.token.test"))
                    .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                    .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo("application/json"))
                    .withHeader("Tema", WireMock.equalTo("PEN"))
                    .withHeader("Nav-Call-Id", WireMock.equalTo("correlationId"))
                    .withHeader("Nav-Consumer-Id", WireMock.equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                    .withRequestBody(
                        WireMock.equalToJson(
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
                    .willReturn(WireMock.ok())
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
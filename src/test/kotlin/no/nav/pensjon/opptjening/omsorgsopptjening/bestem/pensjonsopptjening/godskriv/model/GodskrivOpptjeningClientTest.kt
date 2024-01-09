package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external.PoppClientExecption
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders

class GodskrivOpptjeningClientTest : SpringContextTest.NoKafka() {

    @Autowired
    @Qualifier("godskrivOpptjening")
    private lateinit var godskrivOpptjeningClient: GodskrivOpptjeningClient

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
                    post(urlPathEqualTo(POPP_OMSORG_PATH))
                        .willReturn(serverError())
                )

                assertThrows<PoppClientExecption> {
                    godskrivOpptjeningClient.godskriv(
                        omsorgsyter = "elaboraret",
                        omsorgsÅr = 6669,
                        omsorgstype = DomainOmsorgstype.BARNETRYGD,
                        omsorgsmottaker = "nunc"
                    )
                }.also {
                    assertThat(it.toString()).contains("Feil ved kall")
                }
            }
        }
    }

    @Test
    fun `kaster exception med informasjon om feil dersom kall svarer med spesialkode 512`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.givenThat(
                    post(urlPathEqualTo(POPP_OMSORG_PATH))
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
                    godskrivOpptjeningClient.godskriv(
                        omsorgsyter = "elaboraret",
                        omsorgsÅr = 6669,
                        omsorgstype = DomainOmsorgstype.BARNETRYGD,
                        omsorgsmottaker = "nunc"
                    )
                }.also {
                    assertThat(it.toString()).contains("Feil ved kall")
                }
            }
        }
    }


    @Test
    fun `returnerer ingenting hvis kall går bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.givenThat(
                    post(urlPathEqualTo(POPP_OMSORG_PATH))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/json"))
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
                                        "kilde":"OMSORGSOPPTJENING",
                                        "fnrOmsorgFor":"nunc"
                                    }
                                }
                            """.trimIndent()
                            )
                        )
                        .willReturn(ok())
                )

                assertEquals(
                    Unit, godskrivOpptjeningClient.godskriv(
                        omsorgsyter = "elaboraret",
                        omsorgsÅr = 6669,
                        omsorgstype = DomainOmsorgstype.BARNETRYGD,
                        omsorgsmottaker = "nunc"
                    )
                )
            }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.BESTEM_SAK_PATH
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.WIREMOCK_PORT
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.springframework.http.HttpHeaders
import kotlin.test.assertEquals

class BestemSakKlientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    private val client: BestemSakKlient = BestemSakKlient(
        bestemSakUrl = "${wiremock.baseUrl()}/pen",
        tokenProvider = mock { on { getToken() }.thenReturn(TokenProviderConfig.MOCK_TOKEN) },
        metrics = mock()
    )

    @Test
    fun `kaster exception dersom respons indikerer at noe er feil`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                        .willReturn(
                            WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                    """
                                {
                                    "feil":{
                                        "feilKode":"code",
                                        "feilmelding":"fjas"
                                    },
                                    "sakInformasjonListe":[]
                                }
                            """.trimIndent()
                                )
                        )
                )
                assertThrows<BestemSakClientException>
                {
                    client.bestemSak("blabla")
                }.also {
                    assertThat(it.toString()).contains("Feil ved kall")
                }
            }
        }
    }

    @Test
    fun `kaster exception dersom respons returnerer httpfeil`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                        .willReturn(
                            WireMock.aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                    """
                                {
                                    "feil":{
                                        "feilKode":"400",
                                        "feilmelding":"BAD_REQUEST"
                                    },
                                    "sakInformasjonListe":[]
                                }
                            """.trimIndent()
                                )
                        )
                )
                assertThrows<BestemSakClientException>
                {
                    client.bestemSak("blabla")
                }.also {
                    assertThat(it.toString()).contains("Feil ved kall")
                }
            }
        }
    }

    @Test
    fun `kaster exception dersom respons returnerer mange omsorgssaker`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                        .willReturn(
                            WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                    """
                                {
                                    "feil":null, 
                                    "sakInformasjonListe":[
                                        {
                                            "sakId":"1",
                                            "sakType":"OMSORG",
                                            "sakStatus":"OPPRETTET",
                                            "saksbehandlendeEnhetId":"4100",
                                            "nyopprettet":true,
                                            "tilknyttedeSaker":[]
                                        },
                                        {
                                            "sakId":"2",
                                            "sakType":"OMSORG",
                                            "sakStatus":"OPPRETTET",
                                            "saksbehandlendeEnhetId":"4100",
                                            "nyopprettet":true,
                                            "tilknyttedeSaker":[]
                                        }
                                    ]
                                }
                            """.trimIndent()
                                )
                        )
                )
                assertThrows<BestemSakClientException>
                {
                    client.bestemSak("blabla")
                }.also {
                    assertThat(it.toString()).contains("Feil ved kall")
                }
            }
        }
    }

    @Test
    fun `ignorerer andre saker enn omsorg`() {
        Mdc.scopedMdc(CorrelationId.generate()) {
            Mdc.scopedMdc(InnlesingId.generate()) {
                wiremock.stubFor(
                    WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                        .willReturn(
                            WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                    """
                                {
                                    "feil":null, 
                                    "sakInformasjonListe":[
                                        {
                                            "sakId":"3",
                                            "sakType":"BARNEP",
                                            "sakStatus":"TIL_BEHANDLING",
                                            "saksbehandlendeEnhetId":"2000",
                                            "nyopprettet":false,
                                            "tilknyttedeSaker":[]
                                        },
                                        {
                                            "sakId":"1",
                                            "sakType":"OMSORG",
                                            "sakStatus":"OPPRETTET",
                                            "saksbehandlendeEnhetId":"4100",
                                            "nyopprettet":true,
                                            "tilknyttedeSaker":[]
                                        },
                                        {
                                            "sakId":"2",
                                            "sakType":"ALDER",
                                            "sakStatus":"LOPENDE",
                                            "saksbehandlendeEnhetId":"3000",
                                            "nyopprettet":false,
                                            "tilknyttedeSaker":[]
                                        }
                                    ]
                                }
                            """.trimIndent()
                                )
                        )
                )
                assertEquals(
                    Omsorgssak(
                        sakId = "1",
                        enhet = "4100",
                    ),
                    client.bestemSak("blabla")
                )
            }
        }
    }

    @Test
    fun `returnerer unik omsorgssak hvis kall går bra`() {
        Mdc.scopedMdc(CorrelationId.generate()) { correlationId ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesingId ->
                wiremock.stubFor(
                    WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                        .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer test.token.test"))
                        .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo("application/json"))
                        .withHeader("Nav-Call-Id", WireMock.equalTo(correlationId.toString()))
                        .withHeader("Nav-Consumer-Id", WireMock.equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                        .withHeader("x-correlation-id", WireMock.equalTo(correlationId.toString()))
                        .withHeader("x-innlesing-id", WireMock.equalTo(innlesingId.toString()))
                        .willReturn(
                            WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                    """
                                    {
                                        "feil":null, 
                                        "sakInformasjonListe":[
                                            {
                                                "sakId":"1",
                                                "sakType":"OMSORG",
                                                "sakStatus":"OPPRETTET",
                                                "saksbehandlendeEnhetId":"4100",
                                                "nyopprettet":true,
                                                "tilknyttedeSaker":[]
                                            }
                                        ]
                                    }
                                """.trimIndent()
                                )
                        )
                )
                assertEquals(
                    Omsorgssak(
                        sakId = "1",
                        enhet = "4100",
                    ),
                    client.bestemSak("blabla")
                )
            }
        }
    }
}
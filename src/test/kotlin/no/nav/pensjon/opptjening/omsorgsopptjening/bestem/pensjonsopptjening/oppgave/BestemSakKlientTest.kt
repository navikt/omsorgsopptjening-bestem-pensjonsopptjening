package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class BestemSakKlientTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var client: BestemSakKlient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `kaster exception dersom respons indikerer at noe er feil`() {
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
            assertTrue(it.toString().contains("Feil i respons fra bestem sak"))
        }
    }

    @Test
    fun `kaster exception dersom respons returnerer httpfeil`() {
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
            assertTrue(it.toString().contains("BAD_REQUEST"))
        }
    }

    @Test
    fun `kaster exception dersom respons returnerer mange omsorgssaker`() {
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
            assertTrue(it.toString().contains("Klarte ikke å identifisere unik omsorgssak"))
        }
    }

    @Test
    fun `ignorerer andre saker enn omsorg`() {
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

    @Test
    fun `returnerer unik omsorgssak hvis kall går bra`() {
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
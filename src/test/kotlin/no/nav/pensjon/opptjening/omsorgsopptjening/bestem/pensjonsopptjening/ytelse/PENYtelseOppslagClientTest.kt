package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.ytelse

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.WIREMOCK_PORT
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.løpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.løpendeUføretrygd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.YtelsePeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.YtelseType
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ytelseinformasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.februar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.juli
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mars
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode.Companion.november
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class PENYtelseOppslagClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    private val client: YtelseOppslag = PENYtelseOppslagClient(
        baseUrl = "${wiremock.baseUrl()}/pen",
        tokenProvider = mock { on { getToken() }.thenReturn(TokenProviderConfig.MOCK_TOKEN) },
        restTemplate = RestTemplateBuilder().build()
    )

    @Test
    fun `request inneholder forventede verdier`() {
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        Mdc.scopedMdc(CorrelationId.generate()) { correlation ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesing ->
                val alderspensjon = client.hentLøpendeAlderspensjon(
                    fnr = "fuisset",
                    fraOgMed = januar(2024),
                    tilOgMed = mai(2024)
                )

                val uføretrygd = client.hentLøpendeUføretrygd(
                    fnr = "fuisset",
                    fraOgMed = januar(2024),
                    tilOgMed = mai(2024)
                )

                assertThat(alderspensjon).isEqualTo(
                    Ytelseinformasjon(
                        perioder = emptySet(),
                        rådata = """{"vedtakListe":[]}"""
                    )
                )
                assertThat(uføretrygd).isEqualTo(
                    Ytelseinformasjon(
                        perioder = emptySet(),
                        rådata = """{"uforeperioder":[]}"""
                    )
                )

                wiremock.verify(
                    postRequestedFor(urlPathEqualTo("/pen/api/alderspensjon/vedtak/gjeldende"))
                        .withRequestBody(
                            EqualToJsonPattern(
                                """{"fnr":"fuisset","fomDato":"2024-01-01","tomDato":"2024-05-31"}""".trimIndent(),
                                false,
                                false
                            )
                        )
                        .withHeader("Nav-Call-Id", equalTo(correlation.toString()))
                        .withHeader("x-correlation-id", equalTo(correlation.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesing.toString()))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer ${TokenProviderConfig.MOCK_TOKEN}"))
                )

                wiremock.verify(
                    postRequestedFor(urlPathEqualTo("/pen/api/uforetrygd/vedtak/gjeldende"))
                        .withRequestBody(
                            EqualToJsonPattern(
                                """{"pid":"fuisset","fom":"2024-01-01","tom":"2024-05-31"}""".trimIndent(),
                                false,
                                false
                            )
                        )
                        .withHeader("Nav-Call-Id", equalTo(correlation.toString()))
                        .withHeader("x-correlation-id", equalTo(correlation.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesing.toString()))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer ${TokenProviderConfig.MOCK_TOKEN}"))
                )
            }
        }
    }

    @Test
    fun `deserialiserer respons korrekt og minimerer perioder`() {
        wiremock.løpendeAlderspensjon(
            "fuisset",
            Periode(januar(2024), januar(2024)),
            Periode(februar(2024), juli(2024)),
            Periode(november(2024), desember(2024))
        )
        wiremock.løpendeUføretrygd(
            "fuisset",
            Periode(mars(2021), januar(2025))
        )

        Mdc.scopedMdc(CorrelationId.generate()) { correlation ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesing ->
                val alderspensjon = client.hentLøpendeAlderspensjon(
                    fnr = "fuisset",
                    fraOgMed = januar(2024),
                    tilOgMed = mai(2024)
                )

                val uføretrygd = client.hentLøpendeUføretrygd(
                    fnr = "fuisset",
                    fraOgMed = januar(2024),
                    tilOgMed = mai(2024)
                )

                assertThat(alderspensjon).isEqualTo(
                    Ytelseinformasjon(
                        perioder = setOf(
                            YtelsePeriode(
                                fom = januar(2024),
                                tom = juli(2024),
                                type = YtelseType.ALDERSPENSJON
                            ),
                            YtelsePeriode(
                                fom = november(2024),
                                tom = desember(2024),
                                type = YtelseType.ALDERSPENSJON
                            )
                        ),
                        rådata = alderspensjon.rådata.also {
                            JSONAssert.assertEquals(
                                """
                                    {
                                        "vedtakListe": [
                                            {
                                                "gjelderFomDato": "2024-01-01",
                                                "gjelderTomDato": "2024-01-31"
                                            }, 
                                            {
                                                "gjelderFomDato": "2024-02-01",
                                                "gjelderTomDato": "2024-07-31"
                                            },
                                            {
                                                "gjelderFomDato": "2024-11-01",
                                                "gjelderTomDato": "2024-12-31"
                                            }
                                        ]
                                    }
                                """.trimIndent(),
                                it,
                                true
                            )
                        }.trimIndent()
                    )
                )

                assertThat(uføretrygd).isEqualTo(
                    Ytelseinformasjon(
                        perioder = setOf(
                            YtelsePeriode(
                                fom = mars(2021),
                                tom = januar(2025),
                                type = YtelseType.UFØRETRYGD
                            ),
                        ),
                        rådata = uføretrygd.rådata.also {
                            JSONAssert.assertEquals(
                                """{
                                    "uforeperioder": [
                                        {
                                            "fom":"2021-03-01",
                                            "tom":"2025-01-31"
                                        }
                                    ]
                                   }""".trimIndent(),
                                it,
                                true
                            )
                        }
                    )
                )
            }
        }
    }
}
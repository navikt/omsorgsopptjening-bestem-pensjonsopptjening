package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.WIREMOCK_PORT
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.unntaksperioderMedPliktigEllerFrivilligMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.unntaksperioderUtenMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapsUnntakOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsunntak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.MedlemskapsunntakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.security.mock.oauth2.http.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import kotlin.test.Test

class MedlemskapsUnntakOppslagClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    private val client: MedlemskapsUnntakOppslag = MedlemskapsUnntakOppslagClient(
        url = wiremock.baseUrl(),
        tokenProvider = mock { on { getToken() }.thenReturn(TokenProviderConfig.MOCK_TOKEN) },
        restTemplate = RestTemplateBuilder().build()
    )

    @Test
    fun `kan slå opp informasjon for perioder uten medlemskap`() {
        wiremock.unntaksperioderUtenMedlemskap(
            fnr = "04427625287",
            perioder = setOf(Periode(mai(2024), oktober(2024)))
        )

        val soekRequestBody = SoekRequestBody(
            personident = "04427625287",
            fraOgMed = "2024-01-01",
            tilOgMed = "2024-12-31",
            statuser = listOf("GYLD", "UAVK"),
            type = null,
            ekskluderKilder = null,
            inkluderSporingsinfo = null
        )

        Mdc.scopedMdc(CorrelationId.generate()) { correlation ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesing ->
                val grunnlag = client.hentUnntaksperioder(
                    fnr = "04427625287",
                    fraOgMed = januar(2024),
                    tilOgMed = desember(2024)
                )

                assertThat(grunnlag).isEqualTo(
                    Medlemskapsunntak(
                        ikkeMedlem = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = mai(2024),
                                tilOgMed = oktober(2024),
                            )
                        ),
                        pliktigEllerFrivillig = emptySet(),
                        rådata = wiremock.allServeEvents.single().response.bodyAsString
                    )
                )
                wiremock.verify(
                    postRequestedFor(urlPathEqualTo("/rest/v1/periode/soek"))
                        .withHeader("x-correlation-id", equalTo(correlation.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesing.toString()))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer ${TokenProviderConfig.MOCK_TOKEN}"))
                        .withRequestBody(equalToJson(objectMapper.writeValueAsString(soekRequestBody)))
                )
            }
        }
    }

    @Test
    fun `kan slå opp informasjon for perioder med medlemskap`() {
        wiremock.unntaksperioderMedPliktigEllerFrivilligMedlemskap(
            fnr = "04427625287",
            perioder = setOf(Periode(mai(2024), oktober(2024)))
        )

        Mdc.scopedMdc(CorrelationId.generate()) { correlation ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesing ->
                val grunnlag = client.hentUnntaksperioder(
                    fnr = "04427625287",
                    fraOgMed = januar(2024),
                    tilOgMed = desember(2024)
                )

                val soekRequestBody = SoekRequestBody(
                    personident = "04427625287",
                    fraOgMed = "2024-01-01",
                    tilOgMed = "2024-12-31",
                    statuser = listOf("GYLD", "UAVK"),
                    type = null,
                    ekskluderKilder = null,
                    inkluderSporingsinfo = null
                )

                assertThat(grunnlag).isEqualTo(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = mai(2024),
                                tilOgMed = oktober(2024),
                            )
                        ),
                        rådata = wiremock.allServeEvents.single().response.bodyAsString
                    )
                )
                wiremock.verify(
                    postRequestedFor(urlPathEqualTo("/rest/v1/periode/soek"))
                        .withHeader("x-correlation-id", equalTo(correlation.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesing.toString()))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer ${TokenProviderConfig.MOCK_TOKEN}"))
                        .withRequestBody(equalToJson(objectMapper.writeValueAsString(soekRequestBody)))
                )
            }
        }
    }

    @Test
    fun `trunkerer til og med for å unngå å måtte generer måneder til og med 9999-12-31`() {
        wiremock.unntaksperioderMedPliktigEllerFrivilligMedlemskap(
            fnr = "04427625287",
            perioder = setOf(Periode(mai(2024), oktober(9999))) //default tilOgMed verdi for åpne perioder i MEDL
        )

        Mdc.scopedMdc(CorrelationId.generate()) { correlation ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesing ->
                val grunnlag = client.hentUnntaksperioder(
                    fnr = "04427625287",
                    fraOgMed = januar(2024),
                    tilOgMed = desember(2024)
                )

                assertThat(grunnlag).isEqualTo(
                    Medlemskapsunntak(
                        ikkeMedlem = emptySet(),
                        pliktigEllerFrivillig = setOf(
                            MedlemskapsunntakPeriode(
                                fraOgMed = mai(2024),
                                tilOgMed = desember(2024),
                            )
                        ),
                        rådata = wiremock.allServeEvents.single().response.bodyAsString
                    )
                )
            }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.external

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.TokenProviderConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.mai
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import kotlin.test.Test

class MedlemskapOppslagClientTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var client: MedlemskapOppslagClient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `kan slå opp informasjon om medlemskap`() {
        val response = """
        [
          {
            "unntakId": 0,
            "ident": "04427625287",
            "fraOgMed": "2024-05-09",
            "tilOgMed": "2024-10-09",
            "status": "string",
            "statusaarsak": "string",
            "dekning": "string",
            "helsedel": true,
            "medlem": true,
            "lovvalgsland": "string",
            "lovvalg": "string",
            "grunnlag": "string",
            "sporingsinformasjon": {
              "versjon": 0,
              "registrert": "2024-05-09",
              "besluttet": "2024-10-09",
              "kilde": "string",
              "kildedokument": "string",
              "opprettet": "2024-10-09T10:53:14.596Z",
              "opprettetAv": "string",
              "sistEndret": "2024-10-09T10:53:14.596Z",
              "sistEndretAv": "string"
            },
            "studieinformasjon": {
              "statsborgerland": "string",
              "studieland": "string",
              "delstudie": true,
              "soeknadInnvilget": true
            }
          }
        ]
        """.trimIndent()

        wiremock.givenThat(
            get(urlPathEqualTo("$MEDLEMSKAP_PATH/api/v1/medlemskapsunntak"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(response)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                )
        )


        Mdc.scopedMdc(CorrelationId.generate()) { correlation ->
            Mdc.scopedMdc(InnlesingId.generate()) { innlesing ->
                val grunnlag = client.hentMedlemskapsgrunnlag(
                    fnr = "04427625287",
                    fraOgMed = januar(2024),
                    tilOgMed = desember(2024)
                )

                assertThat(grunnlag).isEqualTo(
                    Medlemskapsgrunnlag(
                        unntaksperioder = listOf(
                            Medlemskapsgrunnlag.Unntaksperiode(
                                fraOgMed = mai(2024),
                                tilOgMed = oktober(2024),
                            )
                        ),
                        rådata = response
                    )
                )
                wiremock.verify(
                    getRequestedFor(urlPathEqualTo("$MEDLEMSKAP_PATH/api/v1/medlemskapsunntak"))
                        .withQueryParam("fraOgMed", equalTo("2024-01-01"))
                        .withQueryParam("tilOgMed", equalTo("2024-12-31"))
                        .withHeader("Nav-Personident", equalTo("04427625287"))
                        .withHeader("x-correlation-id", equalTo(correlation.toString()))
                        .withHeader("x-innlesing-id", equalTo(innlesing.toString()))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer ${TokenProviderConfig.MOCK_TOKEN}"))
                )
            }
        }
    }
}
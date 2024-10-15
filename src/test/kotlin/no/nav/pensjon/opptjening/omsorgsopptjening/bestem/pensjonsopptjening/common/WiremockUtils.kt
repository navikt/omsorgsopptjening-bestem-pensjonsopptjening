package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.POPP_PENSJONSPOENG_PATH
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper
import kotlin.random.Random

/**
 * Velger body-fil basert på identen i requesten.
 * Forenkler stub-oppsett ved å frigjøre testene fra byrden med å konfigurere hvilke identer som skal returneres i
 * hvilken rekkefølge. Dette gjør at testene slipper å forholde seg til interne detaljer om sorteringsrekkefølge o.l
 * ved testoppsett, i tillegg til at det sikrer at en ident alltid får samme respons.
 * Merk at dette bare gjøres dersom [ResponseDefinition.bodyFileName] ikke er satt fra før, slik at man fortsatt står
 * fritt til å returnere det man ønsker (f.eks for å simulere feilsituasjoner).
 */
class PdlIdentToBodyFileTransformer : ResponseDefinitionTransformer() {

    companion object {
        private val fnrToBodyMapping = mapOf(
            "12345678910" to "fnr_1bruk.json",
            "04010012797" to "fnr_samme_fnr_gjeldende_og_historisk.json",
            "01018212345" to "fnr_1bruk_pluss_historisk.json",
            "07081812345" to "fnr_barn_2ar_2020.json",
            "01052012345" to "fnr_barn_0ar_may_2020.json",
            "01122012345" to "fnr_barn_0ar_des_2020.json",
            "03041212345" to "fnr_barn_12ar_2020.json",
            "01019212345" to "fodsel_1freg_0pdl.json",
            "12340378910" to "fnr_barn_17ar_2020.json",
        )
    }

    override fun getName(): String {
        return this::class.qualifiedName!!
    }

    override fun transform(
        p0: Request?,
        p1: ResponseDefinition?,
        p2: FileSource?,
        p3: Parameters?
    ): ResponseDefinition {
        return if (p0!!.url.equals(SpringContextTest.PDL_PATH) && p1!!.bodyFileName == null) {
            val ident = mapper.readValue<JsonNode>(p0.bodyAsString).get("variables").get("ident").textValue()
            ResponseDefinitionBuilder.like(p1)
                .withBodyFile(
                    fnrToBodyMapping[ident]
                        ?: throw RuntimeException("Ident fo body not defined for: $ident, known mappings: $fnrToBodyMapping")
                )
                .build()
        } else {
            p1!!
        }
    }
}

fun wiremockWithPdlTransformer() = WireMockExtension.newInstance()
    .options(
        WireMockConfiguration.wireMockConfig().port(SpringContextTest.WIREMOCK_PORT)
            .extensions(PdlIdentToBodyFileTransformer())
    )
    .build()!!

fun WireMockExtension.stubForPdlTransformer() {
    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.PDL_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.ingenUnntaksperioderForMedlemskap() {
    this.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(SpringContextTest.MEDLEMSKAP_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody("""[]""")
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.unntaksperioderUtenMedlemskap(
    fnr: String,
    perioder: Set<Periode>
) {
    this.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(SpringContextTest.MEDLEMSKAP_PATH))
            .withHeader("Nav-Personident", equalTo(fnr))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(
                        perioder.map {
                            """
                            {
                            "unntakId": ${Random.nextInt()},
                            "ident": "$fnr",
                            "fraOgMed": "${it.min().atDay(1)}",
                            "tilOgMed": "${it.max().atEndOfMonth()}",
                            "status": "GYLD",
                            "statusaarsak": "string",
                            "dekning": "string",
                            "helsedel": true,
                            "medlem": false,
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
                        """.trimIndent()
                        }.toString()
                    )
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.unntaksperioderMedPliktigEllerFrivilligMedlemskap(
    fnr: String,
    perioder: Set<Periode>
) {
    this.stubFor(
        WireMock.get(WireMock.urlPathEqualTo(SpringContextTest.MEDLEMSKAP_PATH))
            .withHeader("Nav-Personident", equalTo(fnr))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(
                        perioder.map {
                            """
                            {
                            "unntakId": ${Random.nextInt()},
                            "ident": "$fnr",
                            "fraOgMed": "${it.min().atDay(1)}",
                            "tilOgMed": "${it.max().atEndOfMonth()}",
                            "status": "GYLD",
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
                        """.trimIndent()
                        }.toString()
                    )
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.ingenPensjonspoeng(fnr: String) {
    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo("$POPP_PENSJONSPOENG_PATH/hent"))
            .withRequestBody(
                equalToJson(
                    """
                {
                  "fnr" : "$fnr"
                }
            """.trimIndent(), true, true
                )
            )
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                            {
                                "pensjonspoeng": []
                            }
                        """.trimIndent()
                    )
            )
    )
}

fun WireMockExtension.bestemSakOk() {
    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.BESTEM_SAK_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-type", "application/json")
                    .withBody(
                        """
                                    {
                                        "feil":null, 
                                        "sakInformasjonListe":[
                                            {
                                                "sakId":"12345",
                                                "sakType":"OMSORG",
                                                "sakStatus":"OPPRETTET",
                                                "saksbehandlendeEnhetId":"4100",
                                                "nyopprettet":false,
                                                "tilknyttedeSaker":[]
                                            }
                                        ]
                                    }
                                """.trimIndent()
                    )
            )
    )
}


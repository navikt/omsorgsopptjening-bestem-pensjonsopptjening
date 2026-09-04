package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.matching.ContainsPattern
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.POPP_PENSJONSPOENG_PATH
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import kotlin.random.Random

/**
 * Navngitte identer for testscenarioer med familiekonstellasjoner, slik at tester kan referere til personer i
 * scenarioet ved navn i stedet for "magiske" fnr-strenger. Body-filene ligger under
 * src/test/resources/__files/fellesbarn/<scenario>/.
 */
object Fellesbarn {

    /**
     * Scenario: Far A + Mor B har tre felles barn (AB1-3), 3-5 år i 2020.
     * Far A har i tillegg to yngre barn med Mor C (AC1, AC2), 0-1 år i 2020.
     */
    object ForeldreFlereFellesBarn {
        const val FAR_A = "10038512345"
        const val MOR_B = "22078812345"
        const val MOR_C = "19058612345"
        const val BARN_AB1 = "15061712345"
        const val BARN_AB2 = "10091612345"
        const val BARN_AB3 = "01121512345"
        const val BARN_AC1 = "08032012345"
        const val BARN_AC2 = "25081912345"
    }

    /**
     * Scenario: Far A + Mor B felles barn (AB1, AB2), Far A + Mor C særkullsbarn (AC1),
     * Mor B + Far D særkullsbarn (BD1). Barn 0-5 år i 2020. AB1 er eldre enn AC1.
     */
    object FellesbarnOgSaerkullsbarn {
        const val FAR_A = "18049112345"
        const val MOR_B = "27069312345"
        const val MOR_C = "05108712345"
        const val FAR_D = "12038012345"
        const val BARN_AB1 = "23041612345"
        const val BARN_AB2 = "14021912345"
        const val BARN_AC1 = "14071812345"
        const val BARN_BD1 = "30111512345"
    }
}

/**
 * Velger body-fil basert på identen i requesten.
 * Forenkler stub-oppsett ved å frigjøre testene fra byrden med å konfigurere hvilke identer som skal returneres i
 * hvilken rekkefølge. Dette gjør at testene slipper å forholde seg til interne detaljer om sorteringsrekkefølge o.l
 * ved testoppsett, i tillegg til at det sikrer at en ident alltid får samme respons.
 * Merk at dette bare gjøres dersom [ResponseDefinition.bodyFileName] ikke er satt fra før, slik at man fortsatt står
 * fritt til å returnere det man ønsker (f.eks for å simulere feilsituasjoner).
 */
class PdlIdentToBodyFileTransformer : ResponseDefinitionTransformerV2 {

    companion object {
        private val fnrToBodyMapping = mapOf(
            "12345678910" to "fnr_1bruk.json",
            "04010012797" to "fnr_samme_fnr_gjeldende_og_historisk.json",
            "01018212345" to "fnr_1bruk_pluss_historisk.json",
            "07081812345" to "fnr_barn_2ar_2020.json",
            "67081812345" to "fnr_barn_2ar_2020.json",
            "01052012345" to "fnr_barn_0ar_may_2020.json",
            "01122012345" to "fnr_barn_0ar_des_2020.json",
            "03041212345" to "fnr_barn_12ar_2020.json",
            "01019212345" to "fodsel_1freg_0pdl.json",
            "12340378910" to "fnr_barn_17ar_2020.json",
            // Scenario: foreldre_flere_felles_barn - Far A + Mor B har tre felles barn (AB1-3), 0-6 år i 2020. Far A har i tillegg to barn med Mor C (AC1, AC2).
            Fellesbarn.ForeldreFlereFellesBarn.FAR_A to "fellesbarn/foreldre_flere_felles_barn/farA.json",
            Fellesbarn.ForeldreFlereFellesBarn.MOR_B to "fellesbarn/foreldre_flere_felles_barn/morB.json",
            Fellesbarn.ForeldreFlereFellesBarn.MOR_C to "fellesbarn/foreldre_flere_felles_barn/morC.json",
            Fellesbarn.ForeldreFlereFellesBarn.BARN_AB1 to "fellesbarn/foreldre_flere_felles_barn/barnAB1.json",
            Fellesbarn.ForeldreFlereFellesBarn.BARN_AB2 to "fellesbarn/foreldre_flere_felles_barn/barnAB2.json",
            Fellesbarn.ForeldreFlereFellesBarn.BARN_AB3 to "fellesbarn/foreldre_flere_felles_barn/barnAB3.json",
            Fellesbarn.ForeldreFlereFellesBarn.BARN_AC1 to "fellesbarn/foreldre_flere_felles_barn/barnAC1.json",
            Fellesbarn.ForeldreFlereFellesBarn.BARN_AC2 to "fellesbarn/foreldre_flere_felles_barn/barnAC2.json",
            // Scenario: fellesbarn_og_saerkullsbarn - Far A + Mor B felles barn (AB1), Far A + Mor C saerkullsbarn (AC1), Mor B + Far D saerkullsbarn (BD1), barn 0-5 år i 2020.
            Fellesbarn.FellesbarnOgSaerkullsbarn.FAR_A to "fellesbarn/fellesbarn_og_saerkullsbarn/farA.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.MOR_B to "fellesbarn/fellesbarn_og_saerkullsbarn/morB.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.MOR_C to "fellesbarn/fellesbarn_og_saerkullsbarn/morC.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.FAR_D to "fellesbarn/fellesbarn_og_saerkullsbarn/farD.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.BARN_AB1 to "fellesbarn/fellesbarn_og_saerkullsbarn/barnAB1.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.BARN_AB2 to "fellesbarn/fellesbarn_og_saerkullsbarn/barnAB2.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.BARN_AC1 to "fellesbarn/fellesbarn_og_saerkullsbarn/barnAC1.json",
            Fellesbarn.FellesbarnOgSaerkullsbarn.BARN_BD1 to "fellesbarn/fellesbarn_og_saerkullsbarn/barnBD1.json",
        )
    }

    override fun getName(): String {
        return this::class.qualifiedName!!
    }

    override fun transform(serveEvent: ServeEvent): ResponseDefinition? {
        val request = serveEvent.request
        val responseDefinition = serveEvent.responseDefinition
        return if (request!!.url.equals(SpringContextTest.PDL_PATH) && responseDefinition!!.bodyFileName == null) {
            val ident = ObjectMapper().readValue(request.bodyAsString, JsonNode::class.java).get("variables").get("ident").textValue()
            ResponseDefinitionBuilder.like(responseDefinition)
                .withBodyFile(
                    fnrToBodyMapping[ident]
                        ?: throw RuntimeException("Ident fo body not defined for: $ident, known mappings: $fnrToBodyMapping")
                )
                .build()
        } else {
            responseDefinition!!
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
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.MEDLEMSKAP_PATH))
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
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.MEDLEMSKAP_PATH))
            .withRequestBody(ContainsPattern(fnr))
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
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.MEDLEMSKAP_PATH))
            .withRequestBody(ContainsPattern(fnr))
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

fun WireMockExtension.ingenLøpendeAlderspensjon() {
    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.PEN_ALDERVEDTAK_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody("""{"vedtakListe":[]}""")
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.løpendeAlderspensjon(
    fnr: String,
    vararg perioder: Periode
) {
    val periodeJson = perioder.joinToString(",") {
        """{"gjelderFomDato":"${it.min().atDay(1)}","gjelderTomDato":"${it.max().atEndOfMonth()}"}"""
    }

    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.PEN_ALDERVEDTAK_PATH))
            .withRequestBody(ContainsPattern(fnr))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody("""{"vedtakListe":[$periodeJson]}""")
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.ingenLøpendeUføretrgyd() {
    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.PEN_UFOREVEDTAK_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody("""{"uforeperioder":[]}""")
                    .withHeader("Content-Type", "application/json")
            )
    )
}

fun WireMockExtension.løpendeUføretrygd(
    fnr: String,
    vararg perioder: Periode
) {
    val periodeJson = perioder.joinToString(",") {
        """{"fom":"${it.min().atDay(1)}","tom":"${it.max().atEndOfMonth()}"}"""
    }

    this.stubFor(
        WireMock.post(WireMock.urlPathEqualTo(SpringContextTest.PEN_UFOREVEDTAK_PATH))
            .withRequestBody(ContainsPattern(fnr))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody("""{"uforeperioder": [$periodeJson]}""")
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


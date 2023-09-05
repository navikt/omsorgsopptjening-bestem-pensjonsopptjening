package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlQuery
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper

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
            val ident = mapper.readValue<PdlQuery>(p0.bodyAsString).variables.ident
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


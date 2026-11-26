package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
class AdminWebApiAuthIntegrationTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `returnerer unauthorized uten bearer token`() {
        mockMvc.perform(
            post("/bestem/rekjor-flere")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("uuidliste", "ikke-en-uuid")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `returnerer unauthorized for token med feil issuer`() {
        val token = tokenForFeilIssuer()

        mockMvc.perform(
            post("/bestem/rekjor-flere")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .param("uuidliste", "ikke-en-uuid")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `slipper gjennom token med tom NAVident og svarer bad request ved ugyldig uuid`() {
        val token = tokenForTomNavident()

        mockMvc.perform(
            post("/bestem/rekjor-flere")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .param("uuidliste", "ikke-en-uuid")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `slipper gjennom tjenestetoken uten NAVident og svarer bad request ved ugyldig uuid`() {
        val token = tokenForTjeneste()

        mockMvc.perform(
            post("/bestem/rekjor-flere")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .param("uuidliste", "ikke-en-uuid")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `slipper gjennom internbruker-token og svarer bad request ved ugyldig uuid`() {
        val token = tokenForInternBruker("Z123456")

        mockMvc.perform(
            post("/bestem/rekjor-flere")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .param("uuidliste", "ikke-en-uuid")
                .param("begrunnelse", "Manuell rekjoring")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `slipper gjennom internbruker-token og markerer uuid som feilet naar den ikke finnes`() {
        val token = tokenForInternBruker("Z123456")
        val ukjentUuid = UUID.randomUUID().toString()

        mockMvc.perform(
            post("/bestem/rekjor-flere")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .param("uuidliste", ukjentUuid)
                .param("begrunnelse", "Manuell rekjoring")
        )
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("$ukjentUuid: Feilet")))
    }


    private fun tokenForInternBruker(navIdent: String): String {
        return testOAuth2Server.issueToken(
            issuerId = ISSUER_ID,
            subject = navIdent,
            audience = TEST_AUDIENCE,
            claims = mapOf(
                "NAVident" to navIdent,
                "azp" to "dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening",
                "appid" to "e35e9f9c-25de-4a77-bf7f-a2bd95f084f5",
            ),
        ).serialize()
    }

    private fun tokenForTjeneste(): String {
        return testOAuth2Server.issueToken(
            issuerId = ISSUER_ID,
            subject = "srv-omsorgsopptjening",
            audience = TEST_AUDIENCE,
            claims = mapOf(
                "azp" to "dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening",
                "appid" to "e35e9f9c-25de-4a77-bf7f-a2bd95f084f5",
            ),
        ).serialize()
    }

    private fun tokenForFeilIssuer(): String {
        return testOAuth2Server.issueToken(
            issuerId = "annen-issuer",
            subject = "Z123456",
            audience = TEST_AUDIENCE,
            claims = mapOf(
                "NAVident" to "Z123456",
                "azp" to "dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening",
            ),
        ).serialize()
    }

    private fun tokenForTomNavident(): String {
        return testOAuth2Server.issueToken(
            issuerId = ISSUER_ID,
            subject = "Z123456",
            audience = TEST_AUDIENCE,
            claims = mapOf(
                "NAVident" to "",
                "azp" to "dev-gcp:team-pen:omsorgsopptjening-bestem-pensjonsopptjening",
                "appid" to "e35e9f9c-25de-4a77-bf7f-a2bd95f084f5",
            ),
        ).serialize()
    }

    companion object {
        private const val ISSUER_ID = "default"
        private const val TEST_AUDIENCE = "test-client-id"
        private val testOAuth2Server = MockOAuth2Server()

        @JvmStatic
        @DynamicPropertySource
        fun registerJwtProperties(registry: DynamicPropertyRegistry) {
            testOAuth2Server.start()
            val wellKnown = testOAuth2Server.wellKnownUrl(ISSUER_ID).toString()
            registry.add("AZURE_APP_WELL_KNOWN_URL") { wellKnown }
            registry.add("AZURE_APP_CLIENT_ID") { TEST_AUDIENCE }
            registry.add("no.nav.security.jwt.issuer.azure.discoveryurl") { wellKnown }
            registry.add("no.nav.security.jwt.issuer.azure.accepted_audience") { TEST_AUDIENCE }
        }

        @JvmStatic
        @AfterAll
        fun tearDownOAuth2Server() {
            testOAuth2Server.shutdown()
        }
    }
}

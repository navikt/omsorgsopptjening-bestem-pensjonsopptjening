package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.MockTokenConfig.Companion.MOCK_TOKEN
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import org.junit.Ignore
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.WireMockSpring
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.web.client.RestClientException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class PdlClientTest {

    @Autowired
    lateinit var pdlService: PdlService

    private val dbContainer = PostgresqlTestContainer.instance

    @BeforeEach
    fun resetWiremock() {
        wiremock.resetAll()
        dbContainer.removeDataFromDB()
    }

    @Test
    fun `Given hentPerson then call pdl one time`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("fnr_1bruk.json")
            )
        )

        pdlService.hentPerson(FNR)

        wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    fun `Given hentPerson Then call pdl with fnr`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("fnr_1bruk.json")
            )
        )

        pdlService.hentPerson(FNR)

        wiremock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)).withRequestBody(containing(FNR)))
    }

    @Test
    fun `Given hentPerson Then call pdl with token and other headers`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("fnr_1bruk.json")
            )
        )

        pdlService.hentPerson(FNR)

        wiremock.verify(
            WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH))
                .withHeader("Authorization", WireMock.equalTo("Bearer $MOCK_TOKEN"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .withHeader("Nav-Consumer-Id", WireMock.equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                .withHeader("Tema", WireMock.equalTo("PEN"))
                .withHeader(
                    "Nav-Call-Id",
                    WireMock.matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
                )
        )
    }

    @Test
    fun `Given PDL return folkeregisteridentifikator When hentPerson Then return pdl response`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("fnr_1bruk.json")
            )
        )

        assertNotNull(pdlService.hentPerson(FNR))
    }

    @Test
    @Ignore
    fun `Given other code than 200 When getting person Then retry 3 times before give up`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(WireMock.aResponse().withStatus(401)))
        assertThrows<RestClientException> { pdlService.hentPerson(FNR) }
        wiremock.verify(4, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    @Ignore
    fun `Given server error When getting person Then retry 3 times before give up`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("error_server_error.json")
        ))

        val error = assertThrows<PdlException> { pdlService.hentPerson(FNR) }
        assertEquals(PdlErrorCode.SERVER_ERROR, error.code)
        wiremock.verify(4, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    fun `Given not found When calling pdl Then throw PdlException`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("error_not_found.json")
        ))

        val error = assertThrows<PdlException> { pdlService.hentPerson(FNR) }
        assertEquals(PdlErrorCode.NOT_FOUND, error.code)
        wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    fun `Given unauthenticated When calling pdl Then throw PdlException`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("error_unauthenticated.json")
        ))

        val error = assertThrows<PdlException> { pdlService.hentPerson(FNR) }
        assertEquals(PdlErrorCode.UNAUTHENTICATED, error.code)
        wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    fun `Given unauthorized When calling pdl Then throw PdlException`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("error_unauthorized.json")
        ))

        val error = assertThrows<PdlException> { pdlService.hentPerson(FNR) }
        assertEquals(PdlErrorCode.UNAUTHORIZED, error.code)
        wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    fun `Given bad request When calling pdl Then throw PdlException`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("error_bad_request.json")
        ))

        val error = assertThrows<PdlException> { pdlService.hentPerson(FNR) }
        assertEquals(PdlErrorCode.BAD_REQUEST, error.code)
        wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }


    companion object {
        const val FNR = "11111111111"
        private const val PDL_PATH = "/graphql"
        private val wiremock = WireMockServer(WireMockSpring.options().port(9991)).also { it.start() }

        @JvmStatic
        @AfterAll
        fun clean() {
            wiremock.stop()
            wiremock.shutdown()
        }
    }

}
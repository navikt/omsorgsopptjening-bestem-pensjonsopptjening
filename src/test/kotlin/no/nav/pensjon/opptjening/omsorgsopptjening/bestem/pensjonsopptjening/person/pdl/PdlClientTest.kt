package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.MockTokenConfig.Companion.MOCK_TOKEN
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.WireMockSpring
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import kotlin.test.assertNotNull

@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class PdlClientTest {

    @Autowired
    lateinit var pdlService: PdlService

    @BeforeEach
    fun resetWiremock() {
        wiremock.resetAll()
    }

    @Test
    fun `Given hentPerson then call pdl one time`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(WireMock.aResponse().withStatus(200)))

        pdlService.hentPerson(FNR)

        wiremock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    @Test
    fun `Given hentPerson then call pdl with fnr`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(WireMock.aResponse().withStatus(200)))

        pdlService.hentPerson(FNR)

        wiremock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)).withRequestBody(containing("$FNR")))
    }

    @Test
    fun `Given hentPerson then call pdl with token and other headers`() {
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(WireMock.aResponse().withStatus(200)))

        pdlService.hentPerson(FNR)

        wiremock.verify(
            WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH))
                .withHeader("Authorization", WireMock.equalTo("Bearer $MOCK_TOKEN"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .withHeader("Nav-Consumer-Id", WireMock.equalTo("omsorgsopptjening-bestem-pensjonsopptjening"))
                .withHeader("Nav-Call-Id", WireMock.matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$"))
        )
    }

    @Test
    fun `Given hentPerson When PDL return folkeregisteridentifikator then return pdl response`(){
        wiremock.stubFor(WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn( WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBodyFile("folkeregisteridentifikator.json")))


        assertNotNull(pdlService.hentPerson(FNR))
    }


    companion object {
        const val FNR = "11111111111"
        const val PDL_PATH = "/graphql"
        private val wiremock = WireMockServer(WireMockSpring.options().port(9991)).also { it.start() }

        @JvmStatic
        @AfterAll
        fun clean() {
            wiremock.stop()
            wiremock.shutdown()
        }
    }

}
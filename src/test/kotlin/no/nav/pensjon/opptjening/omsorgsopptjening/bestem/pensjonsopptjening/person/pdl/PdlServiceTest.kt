package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsarbeidListenerTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.WireMockSpring
import org.springframework.kafka.test.context.EmbeddedKafka

@SpringBootTest(classes = [App::class])
@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
internal class PdlServiceTest {

    @Autowired
    lateinit var pdlService: PdlService

    @BeforeEach
    fun resetWiremock() {
        wiremock.resetAll()
    }

    @Test
    fun `Ingen foedsel - ingen velges`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
            WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("fodsel_0freg_0pdl.json")
        ))

        assertThrows<PdlMottatDataException> { pdlService.hentPerson(FNR) }
    }

        @Test
        fun `Et foedsel fra freg - det velges`() {
            wiremock.stubFor(
                WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fodsel_1freg_0pdl.json")
                ))
            val person =  pdlService.hentPerson(FNR)
            assertEquals(1992, person.fodselsAr)
        }

        @Test
        fun `To foedsel - begge fra pdl - det med nyeste endring velges`() {
            wiremock.stubFor(
                WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fodsel_0freg_2pdl.json")
                ))
            val person =  pdlService.hentPerson(FNR)
            assertEquals(1990, person.fodselsAr)
        }

        @Test
        fun `Tre foedsel - en fra Freg - to fra PDL - kronologisk PDL, Freg, PDL - den fra Freg velges pga foretrukket informasjonsskilde`() {
            wiremock.stubFor(
                WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fodsel_1freg_2pdl.json")
                ))
            val person =  pdlService.hentPerson(FNR)
            assertEquals(1998, person.fodselsAr)
        }

        @Test
        fun `Tre foedsel - to fra Freg - en fra PDL - kronologisk PDL, Freg, Freg - det siste fra Freg velges`() {
            wiremock.stubFor(
                WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fodsel_2freg_1pdl.json")
                ))
            val person =  pdlService.hentPerson(FNR)
            assertEquals(1995, person.fodselsAr)
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

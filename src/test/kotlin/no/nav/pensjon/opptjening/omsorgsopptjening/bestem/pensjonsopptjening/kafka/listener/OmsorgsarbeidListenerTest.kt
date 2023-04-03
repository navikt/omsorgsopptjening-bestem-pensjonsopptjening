package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest.Companion.OMSORGSOPPTJENING_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType.OMSORGSOPPTJENING
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.WireMockSpring
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals


@EmbeddedKafka(partitions = 1, topics = [OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsarbeidListenerTest {

    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    lateinit var omsorgsarbeidProducer: KafkaTemplate<String, String>

    @Autowired
    lateinit var omsorgsopptjeingListener: OmsorgsopptjeningMockListener

    @Autowired
    lateinit var personRepository: PersonRepository


    @BeforeEach
    fun resetWiremock() {
        wiremock.resetAll()
        dbContainer.removeDataFromDB()
    }

    @Test
    fun `Given omsorgsarbeid event Then produce omsorgsopptjening event`() {
        wiremock.stubFor(
            post(urlEqualTo(PDL_PATH))
                .inScenario("hent for voksen og barn")
                .whenScenarioStateIs(STARTED)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk.json")
                )
                .willSetStateTo("Voksen opprettet")
        )
        wiremock.stubFor(
            post(urlEqualTo(PDL_PATH))
                .inScenario("hent for voksen og barn")
                .whenScenarioStateIs("Voksen opprettet")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_barn_2ar_2020.json")
                )
        )

        sendOmsorgsarbeidsSnapshot(
            omsorgsAr = 2020,
            fnr = "12345678910",
            fnrOmsorgFor = "22222222222",
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            messageType = KafkaMessageType.OMSORGSARBEID
        )

        val record = omsorgsopptjeingListener.removeFirstRecord(maxSeconds = 10, messageType = OMSORGSOPPTJENING)
        val omsorgsOpptjening = record!!.value().mapToClass(OmsorgsOpptjening::class.java)

        assertEquals(OpptjeningUtfall.INVILGET, omsorgsOpptjening.utfall)
        assertEquals(2020, omsorgsOpptjening.omsorgsAr)
        assertEquals("12345678910", omsorgsOpptjening.person.fnr)
        assertEquals("22222222222", omsorgsOpptjening.omsorgsmottakereInvilget.first().fnr)
        assertNotNull(omsorgsOpptjening.grunnlag)
        assertNotNull(omsorgsOpptjening.omsorgsopptjeningResultater)
    }

    @Test
    fun `Given two omsorgsarbeid events with two different pdl responses Then update database with the last response from pdl`() {
        wiremock.stubFor(
            post(urlEqualTo(PDL_PATH))
                .inScenario("Opprett eller oppdater person")
                .whenScenarioStateIs(STARTED)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk.json")
                )
                .willSetStateTo("Ny person opprettet")
        )
        wiremock.stubFor(
            post(urlEqualTo(PDL_PATH))
                .inScenario("Opprett eller oppdater person")
                .whenScenarioStateIs("Ny person opprettet")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("fnr_1bruk_pluss_historisk.json")
                )
        )

        sendOmsorgsarbeidsSnapshot(
            omsorgsAr = 2020,
            fnr = "12345678910",
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            messageType = KafkaMessageType.OMSORGSARBEID
        )
        sendOmsorgsarbeidsSnapshot(
            omsorgsAr = 2020,
            fnr = "12345678910",
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            messageType = KafkaMessageType.OMSORGSARBEID
        )

        assertNotNull(omsorgsopptjeingListener.removeFirstRecord(maxSeconds = 10, messageType = OMSORGSOPPTJENING))
        assertNotNull(omsorgsopptjeingListener.removeFirstRecord(maxSeconds = 10, messageType = OMSORGSOPPTJENING))

        val person = personRepository.findPersonByFnr("12345678911")!!
        assertEquals(2, person.alleFnr.size)
        assertEquals("12345678910", person.gjeldendeFnr.fnr)
        assertEquals("12345678911", person.historiskeFnr.first().fnr)
        wiremock.verify(2, postRequestedFor(urlEqualTo(PDL_PATH)))
    }

    private fun sendOmsorgsarbeidsSnapshot(
        omsorgsAr: Int,
        fnr: String,
        fnrOmsorgFor: String? = null,
        omsorgstype: OmsorgsarbeidsType,
        messageType: KafkaMessageType
    ): OmsorgsarbeidsSnapshot {
        val omsorgsarbeidsSnapshot =
            OmsorgsarbeidsSnapshot(
                omsorgsyter = Person(fnr),
                omsorgsAr = omsorgsAr,
                omsorgstype = omsorgstype,
                kjoreHash = "XXX",
                kilde = OmsorgsarbeidsKilde.BARNETRYGD,
                omsorgsArbeidSaker = listOf(
                    OmsorgsArbeidSak(
                        omsorgsarbedUtfort = listOf(
                            OmsorgsArbeid(
                                fom = YearMonth.of(omsorgsAr, Month.JANUARY),
                                tom = YearMonth.of(omsorgsAr, Month.DECEMBER),
                                prosent = 100,
                                omsorgsytere = setOf(Person(fnr)),
                                omsorgsmottaker = fnrOmsorgFor?.let{setOf(Person(fnrOmsorgFor))}?: setOf()
                            )
                        )
                    )
                )
            )

        val omsorgsArbeidKey = OmsorgsArbeidKey(fnr, omsorgsAr, omsorgstype)

        val pr = ProducerRecord(
            OMSORGSOPPTJENING_TOPIC,
            null,
            null,
            omsorgsArbeidKey.mapToJson(),
            omsorgsarbeidsSnapshot.mapToJson(),
            createHeaders(messageType)
        )
        omsorgsarbeidProducer.send(pr).get()

        return omsorgsarbeidsSnapshot
    }

    private fun createHeaders(messageType: KafkaMessageType) = mutableListOf(
        RecordHeader(
            KafkaHeaderKey.MESSAGE_TYPE,
            messageType.name.encodeToByteArray()
        )
    )

    companion object {
        const val OMSORGSOPPTJENING_TOPIC = "omsorgsopptjening"
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
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.OmsorgsarbeidListenerTest.Companion.OMSORGSOPPTJENING_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.util.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.util.mapToJson
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
import kotlin.test.assertEquals


@EmbeddedKafka(partitions = 1, topics = [OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsarbeidListenerTest {

    @Autowired
    lateinit var omsorgsarbeidProducer: KafkaTemplate<String, String>

    @Autowired
    lateinit var omsorgsopptjeingListener: OmsorgsopptjeningMockListener


    @BeforeEach
    fun resetWiremock() {
        wiremock.resetAll()
    }


    @Test
    fun `given omsorgsarbeid event then produce omsorgsopptjening event`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH)).willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("fnr_1freg_bruk.json")
            )
        )

        sendOmsorgsarbeidsSnapshot(
            omsorgsAr = 2020,
            fnr = "12345678910",
            omsorgstype = Omsorgstype.BARNETRYGD,
            messageType = KafkaMessageType.OMSORGSARBEID
        )

        val record = omsorgsopptjeingListener.getRecord(10, OMSORGSOPPTJENING)
        val omsorgsOpptjening = record!!.value().mapToClass(OmsorgsOpptjening::class.java)

        assertEquals(omsorgsOpptjening.invilget, false)
        assertEquals(omsorgsOpptjening.omsorgsAr, 2020)
        assertEquals(omsorgsOpptjening.person.fnr, "12345678910")
        assertNotNull(omsorgsOpptjening.grunnlag)
        assertNotNull(omsorgsOpptjening.omsorgsopptjeningResultater)

        wiremock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(PDL_PATH)))
    }

    private fun sendOmsorgsarbeidsSnapshot(
        omsorgsAr: Int,
        fnr: String,
        omsorgstype: Omsorgstype,
        messageType: KafkaMessageType
    ): OmsorgsarbeidsSnapshot {
        val omsorgsarbeidsSnapshot =
            OmsorgsarbeidsSnapshot(
                omsorgsYter = Person(fnr),
                omsorgsAr = omsorgsAr,
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "XXX",
                kilde = Kilde.BA,
                omsorgsArbeidSaker = listOf()
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
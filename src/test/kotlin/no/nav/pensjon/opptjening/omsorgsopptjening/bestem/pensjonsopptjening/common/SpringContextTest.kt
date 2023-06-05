package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.OMSORGSARBEID_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.Companion.OMSORGSOPPTJENING_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka

@EmbeddedKafka(partitions = 1, topics = [OMSORGSARBEID_TOPIC, OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningProducedMessageListener::class)
class SpringContextTest {

    @Autowired
    lateinit var omsorgsgrunnlagProducer: KafkaTemplate<String, String>

    @Autowired
    lateinit var omsorgsopptjeningListener: OmsorgsopptjeningProducedMessageListener

    @BeforeEach
    fun beforeEach() {
        PostgresqlTestContainer.instance.removeDataFromDB()
    }

    companion object {
        const val OMSORGSARBEID_TOPIC = "omsorgsarbeid"
        const val OMSORGSOPPTJENING_TOPIC = "omsorgsopptjening"
        const val PDL_PATH = "/graphql"
        const val WIREMOCK_PORT = 9991
    }

    data class PdlScenario(
        val inState: String = Scenario.STARTED,
        val body: String,
        val setState: String? = null,
    )


    fun sendOmsorgsgrunnlagKafka(
        omsorgsGrunnlag: OmsorgsGrunnlag
    ) {
        val omsorgsArbeidKey = OmsorgsArbeidKey(
            omsorgsyter = omsorgsGrunnlag.omsorgsyter,
            omsorgsAr = omsorgsGrunnlag.omsorgsAr,
            omsorgsType = omsorgsGrunnlag.omsorgstype
        )

        val pr = ProducerRecord(
            OMSORGSARBEID_TOPIC,
            null,
            null,
            omsorgsArbeidKey.mapToJson(),
            omsorgsGrunnlag.mapToJson(),
            createHeaders(KafkaMessageType.OMSORGSARBEID)
        )
        omsorgsgrunnlagProducer.send(pr).get()
    }

    private fun createHeaders(messageType: KafkaMessageType) = mutableListOf(
        RecordHeader(
            KafkaHeaderKey.MESSAGE_TYPE,
            messageType.name.encodeToByteArray()
        )
    )
}
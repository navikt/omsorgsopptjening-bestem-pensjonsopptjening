package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.WithKafka.Companion.OMSORGSARBEID_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest.WithKafka.Companion.OMSORGSOPPTJENING_TOPIC
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles


sealed class SpringContextTest {

    companion object {
        const val PDL_PATH = "/graphql"
        const val WIREMOCK_PORT = 9991
    }

    @ActiveProfiles("no-kafka")
    @SpringBootTest(classes = [App::class])
    class NoKafka : SpringContextTest() {

    }


    @EmbeddedKafka(partitions = 1, topics = [OMSORGSARBEID_TOPIC, OMSORGSOPPTJENING_TOPIC])
    @SpringBootTest(classes = [App::class])
    @Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningProducedMessageListener::class)
    class WithKafka : SpringContextTest() {

        companion object {
            const val OMSORGSARBEID_TOPIC = "omsorgsarbeid"
            const val OMSORGSOPPTJENING_TOPIC = "omsorgsopptjening"
        }

        @Autowired
        lateinit var omsorgsgrunnlagProducer: KafkaTemplate<String, String>

        fun sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag: OmsorgsGrunnlag
        ) {
            val omsorgsArbeidKey = OmsorgsArbeidKey(
                omsorgsyter = omsorgsGrunnlag.omsorgsyter,
                omsorgsType = omsorgsGrunnlag.omsorgstype
            )

            val pr = ProducerRecord(
                OMSORGSARBEID_TOPIC,
                null,
                null,
                omsorgsArbeidKey.mapToJson(),
                omsorgsGrunnlag.mapToJson(),
                listOf(
                    RecordHeader(
                        KafkaHeaderKey.MESSAGE_TYPE,
                        KafkaMessageType.OMSORGSARBEID.name.toByteArray()
                    )
                )
            )
            omsorgsgrunnlagProducer.send(pr).get()
        }
    }


    @BeforeEach
    fun beforeEach() {
        PostgresqlTestContainer.instance.removeDataFromDB()
    }


    data class PdlScenario(
        val inState: String = Scenario.STARTED,
        val body: String,
        val setState: String? = null,
    )

    fun OmsorgsGrunnlag.toConsumerRecord(): ConsumerRecord<String, String> {
        return ConsumerRecord(
            OMSORGSARBEID_TOPIC,
            1,
            1,
            serialize(
                OmsorgsArbeidKey(
                    omsorgsyter = omsorgsyter,
                    omsorgsType = omsorgstype
                )
            ),
            serialize(this),
        ).apply {
            this.headers().add(
                RecordHeader(
                    KafkaHeaderKey.MESSAGE_TYPE,
                    KafkaMessageType.OMSORGSARBEID.name.toByteArray()
                )
            )
        }
    }
}


fun WireMockExtension.stubPdl(scenario: List<SpringContextTest.PdlScenario>) {
    val name = scenario.sumOf { it.hashCode() }
    scenario.forEach {
        this.stubFor(
            WireMock.post(WireMock.urlEqualTo(SpringContextTest.PDL_PATH))
                .inScenario(name.toString())
                .whenScenarioStateIs(it.inState)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(it.body)
                )
                .willSetStateTo(it.setState)
        )
    }
}
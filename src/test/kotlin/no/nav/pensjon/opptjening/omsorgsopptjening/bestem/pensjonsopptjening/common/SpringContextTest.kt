package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.stubbing.Scenario
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CompletableFuture

@DirtiesContext
sealed class SpringContextTest {

    companion object {
        const val PDL_PATH = "/graphql"
        const val WIREMOCK_PORT = 9991
    }

    @ActiveProfiles("no-kafka")
    @SpringBootTest(classes = [App::class])
    class NoKafka : SpringContextTest() {

        @MockBean
        private lateinit var kafkaTemplate: KafkaTemplate<String, String>

        @PostConstruct
        fun postConstruct() {
            willAnswer { invocation ->
                invocation.getArgument(0, ProducerRecord::class.java).let {
                    CompletableFuture.completedFuture(
                        SendResult(
                            it,
                            RecordMetadata(
                                TopicPartition(
                                    it.topic(),
                                    0
                                ), 0L, 0, 0L, 0, 0
                            )
                        )
                    )
                }
            }.given(kafkaTemplate).send(any<ProducerRecord<String, String>>())
        }
    }


    @EmbeddedKafka(partitions = 1, topics = [Topics.Omsorgsopptjening.NAME])
    @SpringBootTest(classes = [App::class])
    @Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningProducedMessageListener::class)
    class WithKafka : SpringContextTest() {

        @Autowired
        lateinit var omsorgsgrunnlagProducer: KafkaTemplate<String, String>

        fun sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag: OmsorgsgrunnlagMelding
        ) {
            val omsorgsArbeidKey = Topics.Omsorgsopptjening.Key(
                ident = omsorgsGrunnlag.omsorgsyter,
            )

            val pr = ProducerRecord(
                Topics.Omsorgsopptjening.NAME,
                null,
                null,
                omsorgsArbeidKey.mapToJson(),
                omsorgsGrunnlag.mapToJson(),
                listOf(
                    RecordHeader(
                        KafkaMessageType.name,
                        KafkaMessageType.OMSORGSGRUNNLAG.name.toByteArray()
                    ),
                    RecordHeader(
                        CorrelationId.name,
                        "abc".toByteArray()
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

    fun OmsorgsgrunnlagMelding.toConsumerRecord(): ConsumerRecord<String, String> {
        return ConsumerRecord(
            Topics.Omsorgsopptjening.NAME,
            1,
            1,
            serialize(
                Topics.Omsorgsopptjening.Key(
                    ident = omsorgsyter,
                )
            ),
            serialize(this),
        ).apply {
            this.headers().add(
                RecordHeader(
                    KafkaMessageType.name,
                    KafkaMessageType.OMSORGSGRUNNLAG.name.toByteArray()
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
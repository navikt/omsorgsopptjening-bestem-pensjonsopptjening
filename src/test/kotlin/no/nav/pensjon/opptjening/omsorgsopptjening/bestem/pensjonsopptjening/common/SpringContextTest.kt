package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.stubbing.Scenario
import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Application
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.KafkaConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.kafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.BeforeEach
import org.junit.platform.commons.logging.LoggerFactory
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.SendResult
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CompletableFuture

@DirtiesContext
sealed class SpringContextTest {

    companion object {
        const val PDL_PATH = "/graphql"
        const val WIREMOCK_PORT = 9991
    }


    @BeforeEach
    fun beforeEach() {
        PostgresqlTestContainer.instance.removeDataFromDB()
    }

    @SpringBootTest(classes = [Application::class])
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
    @SpringBootTest(classes = [Application::class])
    @ActiveProfiles("kafkaIntegrationTest")
    class WithKafka : SpringContextTest() {

        @Configuration
        @Profile("kafkaIntegrationTest")
        class KafkaSecurityConfig {
            @Bean
            fun securityConfig(): KafkaConfig.SecurityConfig =
                KafkaConfig.SecurityConfig(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT")
        }

        @Autowired
        lateinit var producer: KafkaTemplate<String, String>

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
            producer.send(pr).get()
        }

        @Configuration
        @Profile("kafkaIntegrationTest")
        class OmsorgsopptjeningTopicListener {

            private val records: MutableList<ConsumerRecord<String, String>> = mutableListOf()

            init {
                LoggerFactory.getLogger(this::class.java).error { "THIS IS MY $this" }
            }

            @KafkaListener(
                containerFactory = "listener",
                topics = [Topics.Omsorgsopptjening.NAME],
                groupId = "test-omsorgsopptjening-topic-listener"
            )
            private fun poll(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
                records.add(record)
                ack.acknowledge()
            }

            fun getFirstRecord(waitForSeconds: Int, type: KafkaMessageType): ConsumerRecord<String, String> {
                var secondsPassed = 0
                while (secondsPassed < waitForSeconds && records.none { it.kafkaMessageType() == type }) {
                    Thread.sleep(1000)
                    secondsPassed++
                }

                return records.first { it.kafkaMessageType() == type }.also { records.remove(it) }
            }
        }
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
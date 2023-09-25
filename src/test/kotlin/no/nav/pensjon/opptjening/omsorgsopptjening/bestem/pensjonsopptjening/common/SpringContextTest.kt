package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import jakarta.annotation.PostConstruct
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Application
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.OmsorgsarbeidKafkaConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.io.Serializable
import java.util.UUID
import java.util.concurrent.CompletableFuture

@DirtiesContext
sealed class SpringContextTest {

    companion object {
        const val PDL_PATH = "/graphql"
        const val WIREMOCK_PORT = 9991
        const val BESTEM_SAK_PATH = "/pen/api/bestemsak/v1"
        const val SEND_BREV_PATH = "/pen/api/bestillbrev/todo"
        const val OPPGAVE_PATH = "/api/v1/oppgaver"
        const val POPP_OMSORG_PATH = "/api/omsorg"
        const val POPP_PENSJONSPOENG_PATH = "/api/pensjonspoeng"
    }


    @BeforeEach
    protected open fun beforeEach() {
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
        class KafkaCfg {

            @Value("\${kafka.brokers}")
            private lateinit var kafkaBrokers: String

            @Bean
            fun securityConfig(): OmsorgsarbeidKafkaConfig.SecurityConfig =
                OmsorgsarbeidKafkaConfig.SecurityConfig(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT")

            @Bean
            fun producer(securityConfig: OmsorgsarbeidKafkaConfig.SecurityConfig): KafkaTemplate<String, String> {
                return KafkaTemplate(DefaultKafkaProducerFactory(producerConfig() + securityConfig))
            }

            private fun producerConfig(): Map<String, Serializable> = mapOf(
                ProducerConfig.CLIENT_ID_CONFIG to "omsorgsopptjening-bestem-pensjonsopptjening",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers,
            )
        }

        @Autowired
        lateinit var producer: KafkaTemplate<String, String>

        fun sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag: OmsorgsgrunnlagMelding,
            correlationId: String = UUID.randomUUID().toString()
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
                        CorrelationId.identifier,
                        correlationId.toByteArray()
                    )
                )
            )
            producer.send(pr).get()
        }
    }
}
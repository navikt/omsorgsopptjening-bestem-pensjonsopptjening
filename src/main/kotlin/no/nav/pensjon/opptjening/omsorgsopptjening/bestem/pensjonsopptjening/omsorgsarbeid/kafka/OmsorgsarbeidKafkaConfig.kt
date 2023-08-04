package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.KafkaSecurityConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration


@EnableKafka
@Configuration
@Profile("!no-kafka")
class OmsorgsarbeidKafkaConfig(@Value("\${kafka.brokers}") private val aivenBootstrapServers: String) {
    @Bean
    fun omsorgsArbeidKafkaListenerContainerFactory(kafkaSecurityConfig: KafkaSecurityConfig.Common): ConcurrentKafkaListenerContainerFactory<String, String>? =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
            containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(4L))
            consumerFactory = DefaultKafkaConsumerFactory(
                consumerConfig() + kafkaSecurityConfig.securityConfigs,
                StringDeserializer(),
                StringDeserializer()
            )
        }

    private fun consumerConfig(): Map<String, Any> = mapOf(
        ConsumerConfig.CLIENT_ID_CONFIG to "omsorgsopptjening-bestem-pensjonsopptjening",
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to aivenBootstrapServers,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 1,
    )
}
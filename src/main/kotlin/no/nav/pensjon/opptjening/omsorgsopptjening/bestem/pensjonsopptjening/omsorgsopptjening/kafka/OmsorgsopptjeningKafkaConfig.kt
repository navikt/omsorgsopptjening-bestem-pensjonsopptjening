package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.KafkaSecurityConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.io.Serializable


@EnableKafka
@Configuration
@Profile("!no-kafka")
class OmsorgsopptjeningKafkaConfig(@Value("\${kafka.brokers}") private val aivenBootstrapServers: String) {

    @Bean
    fun omsorgsopptjeningProducerKafkaTemplate(kafkaSecurityConfig: KafkaSecurityConfig.Common): KafkaTemplate<String, String> {
        return KafkaTemplate(DefaultKafkaProducerFactory(omsorgsopptjeningProducerConfig() + kafkaSecurityConfig.securityConfigs))
    }

    private fun omsorgsopptjeningProducerConfig(): Map<String, Serializable> = mapOf(
        ProducerConfig.CLIENT_ID_CONFIG to "omsorgsopptjening-producer-omsorgsarbeid",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to aivenBootstrapServers,
    )
}
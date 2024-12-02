package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

@Configuration
class KafkaConfig {
    @Bean
    fun kafkaProducer(): KafkaTemplate<String, String> {
        return MockKafkaTemplate()
    }
}


class MockKafkaTemplate : KafkaTemplate<String, String>(DefaultKafkaProducerFactory(emptyMap())) {
    override fun send(record: ProducerRecord<String, String>): CompletableFuture<SendResult<String, String>> {
        return record.let {
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
    }
}
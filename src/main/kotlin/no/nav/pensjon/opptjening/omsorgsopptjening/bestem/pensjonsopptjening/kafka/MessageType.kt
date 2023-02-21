package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

fun ConsumerRecord<String, String>.kafkaMessageType(): KafkaMessageType {
    val headers = headers().getHeaders(KafkaHeaderKey.MESSAGE_TYPE)

    assert(headers.isNotEmpty()) {
        log("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, was not found", this)
        "Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, was not found. For more information see secure log"
    }
    assert(headers.size < 2) {
        log("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, had multiple values: $headers", this)
        "Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, had multiple values: $headers. For more information see secure log"
    }
    assert(KafkaMessageType.values().map { it.name }.contains(headers.first())) {
        log("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE} contained the unrecognized value: ${headers.first()}", this)
        "Kafka header ${KafkaHeaderKey.MESSAGE_TYPE} contained the unrecognized value: ${headers.first()}. For more information see secure log"
    }

    return KafkaMessageType.valueOf(headers.first())
}

private fun Headers.getHeaders(key: String) = headers(key).map { String(it.value(), StandardCharsets.UTF_8) }

private val SECURE_LOG = LoggerFactory.getLogger("secure")

private fun log(reason: String, record: ConsumerRecord<String, String>) = SECURE_LOG.error("$reason. For record with key: ${record.key()} and value ${record.value()}. ")
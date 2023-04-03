package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

fun ConsumerRecord<String, String>.kafkaMessageType(): KafkaMessageType {
    val headers = headers().getMessageTypeHeaders()

    return if (headers.isEmpty()) {
        SECURE_LOG.error("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, was not found. For record with key: ${key()} and value ${value()}.")
        throw KafkaMessageTypeException("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, was not found. For more information see secure log")
    } else if (headers.size > 1) {
        SECURE_LOG.error("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, had multiple values: $headers. For record with key: ${key()} and value ${value()}.")
        throw KafkaMessageTypeException("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE}, had multiple values: $headers. For more information see secure log")
    } else if (!KafkaMessageType.values().map { it.name }.contains(headers.first())) {
        SECURE_LOG.error("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE} contained the unrecognized value: ${headers.first()}. For record with key: ${key()} and value ${value()}.")
        throw KafkaMessageTypeException("Kafka header ${KafkaHeaderKey.MESSAGE_TYPE} contained the unrecognized value: ${headers.first()}. For more information see secure log")
    } else {
        KafkaMessageType.valueOf(headers.first())
    }
}

private fun Headers.getMessageTypeHeaders() =
    headers(KafkaHeaderKey.MESSAGE_TYPE)
        .map { String(it.value(), StandardCharsets.UTF_8) }

class KafkaMessageTypeException(message: String) : RuntimeException(message)

private val SECURE_LOG = LoggerFactory.getLogger("secure")
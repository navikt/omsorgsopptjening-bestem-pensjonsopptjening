package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory

fun ConsumerRecord<String, String>.kafkaMessageType(): KafkaMessageType {
    val headers = headers().headers(KafkaMessageType.name)

    return if (headers.count() != 1) {
        throw KafkaMessageTypeResolverException("Could not identify header with name:${KafkaMessageType.name} uniquely, found: $headers. See secure log for details").also {
            SECURE_LOG.error("Could not identify header with name:${KafkaMessageType.name} uniquely, found: $headers. key:${key()}, value:${value()}")
        }
    } else {
        headers().lastHeader(KafkaMessageType.name).let {
            KafkaMessageType.valueOf(String(it.value()))
        }
    }
}

class KafkaMessageTypeResolverException(message: String) : RuntimeException(message)

private val SECURE_LOG = LoggerFactory.getLogger("secure")
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.nio.charset.StandardCharsets

//TODO flere sjekker og logging til secure log hvis noe går til .....
fun ConsumerRecord<String, String>.getMessageType(): KafkaMessageType {
    val messageTypeHeaders = headers().headers(KafkaHeaderKey.MESSAGE_TYPE).toList()
    assert(messageTypeHeaders.isNotEmpty()) { "Kafka header with key ${KafkaHeaderKey.MESSAGE_TYPE} was not found" }
    val messageType = String(headers().headers(KafkaHeaderKey.MESSAGE_TYPE).first().value(), StandardCharsets.UTF_8)
    return KafkaMessageType.valueOf(messageType)
}
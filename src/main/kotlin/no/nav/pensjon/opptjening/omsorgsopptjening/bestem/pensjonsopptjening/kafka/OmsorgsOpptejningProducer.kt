package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class OmsorgsOpptejningProducer(
    @Value("\${OMSORGSOPPTJENING_TOPIC}") private val omsorgsOpptjeningTopic: String,
    private val kafkaTemplate: KafkaTemplate<String, String>

) {

    fun publiserOmsorgsopptejning(omsorgsOpptjeninger: OmsorgsOpptjening) {
        send(omsorgsOpptjeninger.kafkaKey(), omsorgsOpptjeninger.kafkaValue())
    }

    fun send(key: String, value: String) {
        val record = ProducerRecord(omsorgsOpptjeningTopic, null, null, key, value, createHeaders())
        kafkaTemplate.send(record).get(1, TimeUnit.SECONDS)
    }

    private fun createHeaders() = mutableListOf(
        RecordHeader(
            KafkaHeaderKey.MESSAGE_TYPE,
            KafkaMessageType.OMSORGSOPPTJENING.name.encodeToByteArray()
        )
    )
}
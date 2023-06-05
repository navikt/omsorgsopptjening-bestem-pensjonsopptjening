package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.kafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
@Profile("!no-kafka")
class OmsorgsopptjeningProducedMessageListener {

    private val records: MutableList<ConsumerRecord<String, String>> = mutableListOf()

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${OMSORGSOPPTJENING_TOPIC}"],
        groupId = "omsorgsopptjening-produced-messages-group"
    )
    private fun poll(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
        records.add(record)
        ack.acknowledge()
    }

    fun removeFirstRecord(maxSeconds: Int): ConsumerRecord<String, String> {
        var secondsPassed = 0
        while (secondsPassed < maxSeconds && records.none { it.kafkaMessageType() == KafkaMessageType.OMSORGSOPPTJENING }) {
            Thread.sleep(1000)
            secondsPassed++
        }
        return records.firstOrNull { it.kafkaMessageType() == KafkaMessageType.OMSORGSOPPTJENING }
            ?.also { records.remove(it) } ?: throw RuntimeException("No messages of type:${KafkaMessageType.OMSORGSOPPTJENING} to consume")
    }
}
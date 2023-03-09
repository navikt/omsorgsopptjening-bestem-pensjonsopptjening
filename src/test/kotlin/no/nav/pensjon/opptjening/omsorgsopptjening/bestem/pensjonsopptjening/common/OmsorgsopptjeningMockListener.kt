package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.kafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import shaded.com.google.common.util.concurrent.Futures.withTimeout


@Component
class OmsorgsopptjeningMockListener {

    private val records: MutableList<ConsumerRecord<String, String>> = mutableListOf()

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${OMSORGSOPPTJENING_TOPIC}"],
        groupId = "TEST"
    )
    fun consumeOmsorgPGodskriving(hendelse: String, record: ConsumerRecord<String, String>, ack: Acknowledgment) {
        records.add(record)
        ack.acknowledge()
    }

    fun removeFirstRecord(maxSeconds: Int, messageType: KafkaMessageType): ConsumerRecord<String, String>? {
        var secondsPassed = 0
        while (secondsPassed < maxSeconds && records.none { it.kafkaMessageType() == messageType }) {
            Thread.sleep(1000)
            secondsPassed++
        }

        return records.first { it.kafkaMessageType() == messageType }.also {
            records.remove(it)
        }
    }
}
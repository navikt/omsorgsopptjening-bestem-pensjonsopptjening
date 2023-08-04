package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.kafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.platform.commons.logging.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
@Profile("!no-kafka")
class OmsorgsopptjeningProducedMessageListener {

    private val records: MutableList<ConsumerRecord<String, String>> = mutableListOf()

    init {
        LoggerFactory.getLogger(this::class.java).error { "THIS IS MY $this" }
    }

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = [Topics.Omsorgsopptjening.NAME],
        groupId = "omsorgsopptjening-produced-messages-group"
    )
    private fun poll(record: ConsumerRecord<String, String>, ack: Acknowledgment) {
        records.add(record)
        ack.acknowledge()
    }

    fun getFirstRecord(waitForSeconds: Int, type: KafkaMessageType): ConsumerRecord<String, String> {
        var secondsPassed = 0
        while (secondsPassed < waitForSeconds && records.none { it.kafkaMessageType() == type }) {
            Thread.sleep(1000)
            secondsPassed++
        }

        return records.first { it.kafkaMessageType() == type }.also { records.remove(it) }
    }
}
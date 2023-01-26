package no.nav.pensjon.opptjening.omsorgsarbeid.konsument.omsorgsarbeid.common

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OmsorgsopptjeningMockListener {

    private val records: MutableList<ConsumerRecord<String, String>> = mutableListOf()

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${OMSORGSOPPTJENING_TOPIC}"],
        groupId = "TEST"
    )
    fun consumeOmsorgPGodskriving(hendelse: String, consumerRecord: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        logger.info("Konsumerer omsorgsmelding: ${consumerRecord.key()}, ${consumerRecord.value()}")
        records.add(consumerRecord)
    }

    fun getRecord(waitForSeconds: Int): ConsumerRecord<String, String>? {
        var secondsPassed = 0
        while (secondsPassed < waitForSeconds && records.size < 1) {
            Thread.sleep(1000)
            secondsPassed++
        }

        return records.removeFirstOrNull()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OmsorgsopptjeningMockListener::class.java)
    }
}
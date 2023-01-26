package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OmsorgsarbeidListener(registry: MeterRegistry) {
    private val antallLesteMeldinger = registry.counter("omsorgsArbeidListener", "antall", "lest")

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${OMSORGSARBEID_TOPIC}"],
        groupId = "\${OMSORGP_GODSKRIVING_GROUP_ID}"
    )
    fun consumeOmsorgPGodskriving(
        hendelse: String,
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        antallLesteMeldinger.increment()
        SECURE_LOG.info("Konsumerer omsorgsmelding: ${consumerRecord.key()}, ${consumerRecord.value()}")

        convertToOmsorgsArbeid(consumerRecord.value())
        convertToOmsorgsArbeidKey(consumerRecord.key())
        acknowledgment.acknowledge()
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secureLog")
    }
}
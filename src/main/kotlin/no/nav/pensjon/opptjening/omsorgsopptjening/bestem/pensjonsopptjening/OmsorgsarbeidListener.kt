package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory.OmsorgsArbeidSakFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.FastsettOmsorgsOpptjening
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
        topics = ["\${OMSORGSOPPTJENING_TOPIC}"],
        groupId = "\${OMSORGSOPPTJENING_BESTEM_GROUP_ID}"
    )
    fun consumeOmsorgPGodskriving(
        hendelse: String,
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        antallLesteMeldinger.increment()
        SECURE_LOG.info("Konsumerer omsorgsmelding: ${consumerRecord.key()}, ${consumerRecord.value()}")

        val key = convertToOmsorgsArbeidKey(consumerRecord.key())
        val value = convertToOmsorgsArbeid(consumerRecord.value())

        SECURE_LOG.info("Mappet omsorgsmelding til: key: $key , value: $value")

        val omsorgsArbeidSak = OmsorgsArbeidSakFactory.createOmsorgsArbeidSak(value)
        val omsorgsOpptjeninger = FastsettOmsorgsOpptjening.fastsettOmsorgsOpptjening(omsorgsArbeidSak, value.omsorgsAr.toInt())

        omsorgsOpptjeninger.forEach{
            SECURE_LOG.info("Person: ${it.person}")
            SECURE_LOG.info("Opptjening: ${ObjectMapper().writeValueAsString(it)}")
        }

        acknowledgment.acknowledge()
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsArbeidService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsopptjeningsService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OmsorgsarbeidListener(
    registry: MeterRegistry,
    private val omsorgsopptjeningsService: OmsorgsopptjeningsService,
    private val omsorgsArbeidService: OmsorgsArbeidService,
) {

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

        if (consumerRecord.kafkaMessageType() == KafkaMessageType.OMSORGSARBEID) {
            SECURE_LOG.info("Behandler: ${consumerRecord.value()}")

            omsorgsopptjeningsService.behandlOmsorgsarbeid(
                omsorgsArbeidService.createAndSaveOmsorgasbeidsSnapshot(map(consumerRecord.value()))
            )
        }

        acknowledgment.acknowledge()
    }

    private fun map(kafkaMessage: String) = kafkaMessage.mapToClass(OmsorgsarbeidsSnapshot::class.java)

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

}
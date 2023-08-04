package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Profile("!no-kafka")
class OmsorgsarbeidListener(
    private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = [Topics.Omsorgsopptjening.NAME],
        groupId = "\${OMSORGSOPPTJENING_BESTEM_GROUP_ID}"
    )
    fun poll(
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        when (consumerRecord.kafkaMessageType()) {
            KafkaMessageType.OMSORGSGRUNNLAG -> {
                Mdc.scopedMdc(CorrelationId.name, consumerRecord.getOrCreateCorrelationId()) {
                    log.info("Prosesserer melding")
                    omsorgsarbeidRepo.persist(
                        PersistertKafkaMelding(
                            melding = consumerRecord.value(),
                            correlationId = Mdc.getOrCreateCorrelationId(),
                        )
                    )
                }
            }

            else -> {
                //NOOP
            }
        }
        acknowledgment.acknowledge()
    }

    private fun ConsumerRecord<*, *>.getOrCreateCorrelationId(): String {
        return headers().lastHeader(CorrelationId.name)?.let { String(it.value()) } ?: CorrelationId.generate()
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.VurderGodskrivOmsorgsopptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToClass
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Profile("!no-kafka")
@Component
class OmsorgsarbeidListener(
    registry: MeterRegistry,
    private val automatiskGodskrivOmsorgsopptjeningService: VurderGodskrivOmsorgsopptjeningService,
    private val omsorgsGrunnlagService: OmsorgsgrunnlagService,
) {

    private val antallLesteMeldinger = registry.counter("omsorgsArbeidListener", "antall", "lest")

    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${OMSORGSARBEID_TOPIC}"],
        groupId = "\${OMSORGSOPPTJENING_BESTEM_GROUP_ID}"
    )
    fun poll(
        hendelse: String,
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        antallLesteMeldinger.increment()
        SECURE_LOG.info("Konsumerer omsorgsmelding: ${consumerRecord.key()}, ${consumerRecord.value()}")

        if (consumerRecord.kafkaMessageType() == KafkaMessageType.OMSORGSARBEID) {
            SECURE_LOG.info("Behandler: ${consumerRecord.value()}")

            automatiskGodskrivOmsorgsopptjeningService.vurder(
                omsorgsGrunnlag = omsorgsGrunnlagService.berik(
                    consumerRecord.value().mapToClass(OmsorgsGrunnlag::class.java)
                )
            )
        }
        acknowledgment.acknowledge()
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

}
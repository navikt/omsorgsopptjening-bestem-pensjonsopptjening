package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka.OmsorgsopptjeningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Profile("!no-kafka")
@Component
class OmsorgsarbeidListener(
    private val omsorgsarbeidMessageHandler: OmsorgsarbeidMessageHandler,
    private val omsorgsopptjeningProducer: OmsorgsopptjeningProducer,
) {
    @KafkaListener(
        containerFactory = "omsorgsArbeidKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${OMSORGSARBEID_TOPIC}"],
        groupId = "\${OMSORGSOPPTJENING_BESTEM_GROUP_ID}"
    )
    fun poll(
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        omsorgsarbeidMessageHandler.handle(consumerRecord).forEach {
            when(it.utfall){
                is AutomatiskGodskrivingUtfall.Avslag -> {
                    //TODO
                }
                is AutomatiskGodskrivingUtfall.Innvilget -> {
                    omsorgsopptjeningProducer.send(it)
                }
            }
        }
        acknowledgment.acknowledge()
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

}
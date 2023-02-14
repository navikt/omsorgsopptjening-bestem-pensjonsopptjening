package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OmsorgsOpptejningProducer(kafkaTemplate: KafkaTemplate<String, String>) {

}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka.OmsorgsopptjeningProducer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC.MDCCloseable
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Profile("!no-kafka")
@Component
class OmsorgsarbeidListener(
    private val omsorgsarbeidMessageHandler: OmsorgsarbeidMessageHandler,
    private val omsorgsopptjeningProducer: OmsorgsopptjeningProducer,
    private val registry: MeterRegistry
) {
    private val antallLesteMeldinger = registry.counter("omsorgsArbeid", "antall", "lest")
    private val antallInnvilgedeOpptjeninger = registry.counter("opptjeninger", "antall", "innvilget")
    private val antallAvslaatteOpptjeninger = registry.counter("opptjeninger", "antall", "avslaatt")

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
        antallLesteMeldinger.increment()

        when (consumerRecord.kafkaMessageType()) {
            KafkaMessageType.OMSORGSGRUNNLAG -> {
                Mdc.scopedMdc(CorrelationId.name, consumerRecord.getOrCreateCorrelationId()){
                    omsorgsarbeidMessageHandler.handle(deserialize(consumerRecord.value())).forEach {
                        when (it.erInnvilget()) {
                            true -> {
                                håndterInnvilgelse(it)
                                antallInnvilgedeOpptjeninger.increment()
                            }

                            false -> {
                                håndterAvslag(it)
                                antallAvslaatteOpptjeninger.increment()
                            }
                        }
                    }
                }
            }

            else -> {
                TODO()
            }
        }
        acknowledgment.acknowledge()
    }

    private fun håndterInnvilgelse(behandling: FullførtBehandling) {
        omsorgsopptjeningProducer.send(behandling)
    }

    private fun håndterAvslag(behandling: FullførtBehandling) {
        behandling
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

    private fun ConsumerRecord<*, *>.getOrCreateCorrelationId(): String {
        return headers().lastHeader(CorrelationId.name)?.let { String(it.value()) } ?: CorrelationId.generate()
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class OmsorgsarbeidKafkaListener(
    private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @KafkaListener(
        containerFactory = "listener",
        topics = [Topics.Omsorgsopptjening.NAME],
        groupId = "omsorgsopptjening-bestem-pensjonsopptjening"
    )
    fun poll(
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        deserialize<OmsorgsgrunnlagMelding>(consumerRecord.value()).also {
            Mdc.scopedMdc(it.correlationId) { _ ->
                Mdc.scopedMdc(it.innlesingId) { _ ->
                    log.info("Prosesserer melding")
                    omsorgsarbeidRepo.persist(
                        OmsorgsarbeidMelding(innhold = it)
                    )
                }
            }
        }
        acknowledgment.acknowledge()
    }
}
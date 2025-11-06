package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.metrics.OmsorgsarbeidListenerMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

@Component
@Profile("dev-gcp", "prod-gcp", "kafkaIntegrationTest")
class PersongrunnlagKafkaListener(
    private val persongrunnlagRepo: PersongrunnlagRepo,
    private val omsorgsarbeidListenerMetricsMåling: OmsorgsarbeidListenerMetrikker
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @KafkaListener(
        containerFactory = "listener",
        topics = [$$"${OMSORGSOPPTJENING_TOPIC}"],
        groupId = $$"${OMSORGSOPPTJENING_CONSUMER_GROUP}"
    )
    fun poll(
        consumerRecord: List<ConsumerRecord<String, String>>,
        acknowledgment: Acknowledgment
    ) {
        val deserialisert = consumerRecord.map { deserialize<PersongrunnlagMeldingKafka>(it.value()) }
        deserialisert.map { PersongrunnlagMelding.Lest(it) }
            .also { leste ->
                persongrunnlagRepo.lagre(leste)
                log.info("Meldinger med offset: min(${consumerRecord.minOf { it.offset() }}), max(${consumerRecord.maxOf { it.offset() }}), antall: ${consumerRecord.count()} prosessert.")
            }

        acknowledgment.acknowledge()
        deserialisert.forEach { omsorgsarbeidListenerMetricsMåling.oppdater { it } }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.toKafka
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsopptjeningInnvilgetMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class OmsorgsopptjeningProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    fun send(behandling: FullførtBehandling) {
        require(behandling.utfall is BehandlingUtfall.Innvilget) { "Should only send messages for utfall: ${BehandlingUtfall.Innvilget::class.java}" }

        val key = Topics.Omsorgsopptjening.Key(
            ident = behandling.omsorgsyter
        )
        val value = OmsorgsopptjeningInnvilgetMelding(
            omsorgsAr = behandling.omsorgsAr,
            omsorgsyter = behandling.omsorgsyter,
            omsorgsmottaker = behandling.omsorgsmottaker,
            kilde = behandling.kilde().toKafka(),
            omsorgstype = behandling.omsorgstype.toKafka()
        )

        send(key.mapToJson(), value.mapToJson())
    }

    private fun send(key: String, value: String) {
        val record = ProducerRecord(Topics.Omsorgsopptjening.NAME, null, null, key, value, createHeaders())
        kafkaTemplate.send(record).get(1, TimeUnit.SECONDS)
    }

    private fun createHeaders() = mutableListOf(
        RecordHeader(
            KafkaMessageType.name,
            KafkaMessageType.OPPTJENING.name.encodeToByteArray(),
        ),
        RecordHeader(
            CorrelationId.name,
            Mdc.getOrCreateCorrelationId().encodeToByteArray()
        )
    )
}
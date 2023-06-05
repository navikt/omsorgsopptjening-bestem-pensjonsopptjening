package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.toKafka
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaHeaderKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsopptjeningInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsopptjeningInnvilgetKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Profile("!no-kafka")
@Component
class OmsorgsopptjeningProducer(
    @Value("\${OMSORGSOPPTJENING_TOPIC}") private val omsorgsOpptjeningTopic: String,
    private val kafkaTemplate: KafkaTemplate<String, String>

) {

    fun send(behandling: FullførtBehandling) {
        require(behandling.utfall is AutomatiskGodskrivingUtfall.Innvilget){"Should only send messages for utfall: ${AutomatiskGodskrivingUtfall.Innvilget::class.java}"}

        val key = OmsorgsopptjeningInnvilgetKey(
            omsorgsAr = behandling.omsorgsAr,
            omsorgsyter = behandling.omsorgsyter.fnr

        )
        val value = OmsorgsopptjeningInnvilget(
            omsorgsAr = behandling.omsorgsAr,
            omsorgsyter = behandling.omsorgsyter.fnr,
            omsorgsmottaker = behandling.omsorgsmottaker()!!.fnr,
            kilde = behandling.kilde().toKafka(),
            omsorgstype = behandling.omsorgstype.toKafka()
        )

        send(key.mapToJson(), value.mapToJson())
    }

    private fun send(key: String, value: String) {
        val record = ProducerRecord(omsorgsOpptjeningTopic, null, null, key, value, createHeaders())
        kafkaTemplate.send(record).get(1, TimeUnit.SECONDS)
    }

    private fun createHeaders() = mutableListOf(
        RecordHeader(
            KafkaHeaderKey.MESSAGE_TYPE,
            KafkaMessageType.OMSORGSOPPTJENING.name.encodeToByteArray()
        )
    )
}
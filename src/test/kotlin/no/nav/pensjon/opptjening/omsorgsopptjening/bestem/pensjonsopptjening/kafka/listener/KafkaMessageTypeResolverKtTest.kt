package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.KafkaMessageTypeResolverException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.kafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord.NULL_SIZE
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class KafkaMessageTypeResolverKtTest {

    @Test
    fun `Given MESSAGE_TYPE header with value in KafkaMessageType When retrieving kafkaMessageType Then return message type`() {
        val consumerRecord = createConsumerRecord(
            RecordHeaders(
                mutableListOf(
                    createHeader(KafkaMessageType.name, KafkaMessageType.OPPTJENING.name),
                    createHeader("OtherHeader", "OtherValue")
                )
            )
        )

        val messageType = consumerRecord.kafkaMessageType()
        assertEquals(KafkaMessageType.OPPTJENING, messageType)
    }

    @Test
    fun `Given no header with key MESSAGE_TYPE When retrieving kafkaMessageType Then throw error`() {
        val consumerRecord = createConsumerRecord(RecordHeaders())

        val error = assertThrows<KafkaMessageTypeResolverException> { consumerRecord.kafkaMessageType() }

        assertTrue(error.message!!.contains("Could not identify header with name:MESSAGE_TYPE uniquely"))
    }

    @Test
    fun `Given more than one header with key MESSAGE_TYPE When retrieving kafkaMessageType Then throw error`() {
        val consumerRecord = createConsumerRecord(
            RecordHeaders(
                mutableListOf(
                    createHeader(KafkaMessageType.name, KafkaMessageType.OPPTJENING.name),
                    createHeader(KafkaMessageType.name, KafkaMessageType.OMSORGSGRUNNLAG.name),
                )
            )
        )

        val error = assertThrows<KafkaMessageTypeResolverException> { consumerRecord.kafkaMessageType() }

        assertTrue(error.message!!.contains("Could not identify header with name:MESSAGE_TYPE uniquely"))
    }

    @Test
    fun `Given unrecognized value in header key MESSAGE_TYPE When retrieving kafkaMessageType Then throw error`() {
        val consumerRecord = createConsumerRecord(
            RecordHeaders(
                mutableListOf(
                    createHeader(KafkaMessageType.name, "Bogus"),
                )
            )
        )

        assertThrows<IllegalArgumentException> { consumerRecord.kafkaMessageType() }
    }

    private fun createConsumerRecord(recordHeaders: RecordHeaders) =
        ConsumerRecord(
            "test",
            1,
            1L,
            ConsumerRecord.NO_TIMESTAMP,
            TimestampType.NO_TIMESTAMP_TYPE,
            NULL_SIZE,
            NULL_SIZE,
            "Test",
            "test",
            recordHeaders,
            Optional.empty()
        )

    private fun createHeader(headerKey: String, headerValue: String) =
        RecordHeader(
            headerKey,
            headerValue.encodeToByteArray()
        )
}
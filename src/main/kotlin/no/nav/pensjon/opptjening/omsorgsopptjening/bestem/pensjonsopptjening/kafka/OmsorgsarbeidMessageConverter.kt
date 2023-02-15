package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.apache.kafka.clients.consumer.ConsumerRecord

private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

internal fun ConsumerRecord<String, String>.getOmsorgsarbeidsSnapshot() = mapper.readValue(value(), OmsorgsarbeidsSnapshot::class.java)

internal fun ConsumerRecord<String, String>.getOmsorgsArbeidKey() = mapper.readValue(key(), OmsorgsArbeidKey::class.java)
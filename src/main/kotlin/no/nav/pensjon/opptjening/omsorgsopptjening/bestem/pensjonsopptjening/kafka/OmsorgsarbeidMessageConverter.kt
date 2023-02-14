package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import org.apache.kafka.clients.consumer.ConsumerRecord

private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

internal fun ConsumerRecord<String, String>.getOmsorgsArbeidValue() = mapper.readValue(value(), OmsorgsArbeid::class.java)

internal fun ConsumerRecord<String, String>.getOmsorgsArbeidKey() = mapper.readValue(key(), OmsorgsArbeidKey::class.java)
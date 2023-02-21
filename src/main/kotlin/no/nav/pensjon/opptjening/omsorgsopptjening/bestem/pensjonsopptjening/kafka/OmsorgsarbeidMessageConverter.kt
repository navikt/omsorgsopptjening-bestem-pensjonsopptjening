package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.util.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.apache.kafka.clients.consumer.ConsumerRecord

internal fun ConsumerRecord<String, String>.getOmsorgsarbeidsSnapshot() = value().mapToClass(OmsorgsarbeidsSnapshot::class.java)

internal fun ConsumerRecord<String, String>.getOmsorgsArbeidKey() = key().mapToClass(OmsorgsArbeidKey::class.java)
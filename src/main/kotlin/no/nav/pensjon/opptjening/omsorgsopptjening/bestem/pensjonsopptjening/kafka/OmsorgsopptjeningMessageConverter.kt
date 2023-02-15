package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.util.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person

private val mapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun OmsorgsOpptjening.createKafkaKey(): String {
    return OmsorgsOpptjeningKey(omsorgsAr, person.gjeldendeFnr.fnr, invilget).mapToJson()
}

fun OmsorgsOpptjening.createKafkaValue(): String {
    val omsorgsOpptjeningMessage =
        no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening(
            omsorgsAr = omsorgsAr,
            person = Person(person.gjeldendeFnr.fnr),
            grunnlag = grunnlag,
            omsorgsopptjeningResultater = omsorgsopptjeningResultater.mapToJson(),
            invilget = invilget
        )
    return omsorgsOpptjeningMessage.mapToJson()
}
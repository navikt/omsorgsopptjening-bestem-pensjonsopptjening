package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.util.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person


fun OmsorgsOpptjening.kafkaKey(): String = OmsorgsOpptjeningKey(omsorgsAr, person.gjeldendeFnr.fnr!!, invilget).mapToJson()

fun OmsorgsOpptjening.kafkaValue(): String =
    no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening(
        omsorgsAr = omsorgsAr,
        person = Person(person.gjeldendeFnr.fnr!!),
        grunnlag = grunnlag,
        omsorgsopptjeningResultater = omsorgsopptjeningResultater.mapToJson(),
        invilget = invilget
    ).mapToJson()
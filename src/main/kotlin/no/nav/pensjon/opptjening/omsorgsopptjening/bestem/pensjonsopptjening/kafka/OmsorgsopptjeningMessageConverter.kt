package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.paragraf.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.util.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OpptjeningAvgjorelse
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person


fun OmsorgsOpptjening.kafkaKey(): String = OmsorgsOpptjeningKey(omsorgsAr, person.gjeldendeFnr.fnr!!, mapAvgjorelse(utfall)).mapToJson()

fun OmsorgsOpptjening.kafkaValue(): String =
    no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening(
        omsorgsAr = omsorgsAr,
        person = Person(person.gjeldendeFnr.fnr!!),
        omsorgsmottakereInvilget = omsorgsmottakereInvilget.map { Person(it.gjeldendeFnr.fnr!!) },
        grunnlag = grunnlag,
        omsorgsopptjeningResultater = omsorgsopptjeningResultater.mapToJson(),
        avgjorelse = mapAvgjorelse(utfall)
    ).mapToJson()

private fun mapAvgjorelse(utfall: Utfall): OpptjeningAvgjorelse =
    when (utfall) {
        Utfall.INVILGET -> OpptjeningAvgjorelse.INVILGET
        Utfall.AVSLAG -> OpptjeningAvgjorelse.AVSLAG
        Utfall.SAKSBEHANDLING -> OpptjeningAvgjorelse.SAKSBEHANDLING
    }
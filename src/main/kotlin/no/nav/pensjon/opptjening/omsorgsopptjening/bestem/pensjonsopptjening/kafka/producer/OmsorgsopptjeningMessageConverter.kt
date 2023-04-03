package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper.OmsorgsarbeidSnapshotMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OpptjeningUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person


fun OmsorgsOpptjening.kafkaKey(): String = OmsorgsOpptjeningKey(omsorgsAr, person.gjeldendeFnr.fnr, mapUtfall(utfall)).mapToJson()

fun OmsorgsOpptjening.kafkaValue(): String = no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening(
        omsorgsAr = omsorgsAr,
        person = Person(person.gjeldendeFnr.fnr),
        omsorgsmottakereInvilget = omsorgsmottakereInvilget.map { Person(it.gjeldendeFnr.fnr) },
        grunnlag = OmsorgsarbeidSnapshotMapper.map(grunnlag),
        omsorgsopptjeningResultater = omsorgsopptjeningResultater.mapToJson(),
        utfall = mapUtfall(utfall)
    ).mapToJson()

private fun mapUtfall(utfall: Utfall): OpptjeningUtfall =
    when (utfall) {
        Utfall.INVILGET -> OpptjeningUtfall.INVILGET
        Utfall.AVSLAG -> OpptjeningUtfall.AVSLAG
        Utfall.SAKSBEHANDLING -> OpptjeningUtfall.SAKSBEHANDLING
        Utfall.MANGLER_ANNEN_OMSORGSYTER -> OpptjeningUtfall.SAKSBEHANDLING //TODO Må endres
    }
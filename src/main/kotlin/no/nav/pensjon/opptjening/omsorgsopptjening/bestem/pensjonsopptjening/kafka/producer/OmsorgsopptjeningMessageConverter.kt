package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsarbeidSnapshotMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening as KafkaOmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OpptjeningUtfall as KafkaUtfall


fun OmsorgsOpptjening.createKafkaKey(): String = OmsorgsOpptjeningKey(
    omsorgsAr = omsorgsAr,
    omsorgsyter = omsorgsyter.gjeldendeFnr.fnr,
    utfall = mapToKafkaUtfall(utfall)
).mapToJson()

fun OmsorgsOpptjening.createKafkaValue(): String = KafkaOmsorgsOpptjening(
    omsorgsAr = omsorgsAr,
    omsorgsyter = Person(omsorgsyter.gjeldendeFnr.fnr),
    omsorgsmottakereInvilget = omsorgsmottakereInvilget.map { Person(it.gjeldendeFnr.fnr) },
    grunnlag = OmsorgsarbeidSnapshotMapper.map(grunnlag),
    vilkarsResultat = vilkarsResultat.mapToJson(),
    utfall = mapToKafkaUtfall(utfall)
).mapToJson()

private fun mapToKafkaUtfall(utfall: Utfall): KafkaUtfall =
    when (utfall) {
        Utfall.INVILGET -> KafkaUtfall.INVILGET
        Utfall.AVSLAG -> KafkaUtfall.AVSLAG
        Utfall.SAKSBEHANDLING -> KafkaUtfall.SAKSBEHANDLING
        Utfall.MANGLER_ANNEN_OMSORGSYTER -> KafkaUtfall.MANGLER_INFORMASJON_OM_ANNEN_OMSORGSYTER
    }
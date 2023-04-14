package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper.OmsorgsarbeidSnapshotMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OpptjeningUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person


fun OmsorgsOpptjening.kafkaKey(): String = OmsorgsOpptjeningKey(
    omsorgsAr = omsorgsAr,
    omsorgsyter = omsorgsyter.gjeldendeFnr.fnr,
    utfall = mapUtfall(utfall)
).mapToJson()

fun OmsorgsOpptjening.kafkaValue(): String = no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening(
        omsorgsAr = omsorgsAr,
        omsorgsyter = Person(omsorgsyter.gjeldendeFnr.fnr),
        omsorgsmottakereInvilget = omsorgsmottakereInvilget.map { Person(it.gjeldendeFnr.fnr) },
        grunnlag = OmsorgsarbeidSnapshotMapper.map(grunnlag),
        vilkarsResultat = vilkarsResultat.mapToJson(),
        utfall = mapUtfall(utfall)
    ).mapToJson()

private fun mapUtfall(utfall: Utfall): OpptjeningUtfall =
    when (utfall) {
        Utfall.INVILGET -> OpptjeningUtfall.INVILGET
        Utfall.AVSLAG -> OpptjeningUtfall.AVSLAG
        Utfall.SAKSBEHANDLING -> OpptjeningUtfall.SAKSBEHANDLING
        Utfall.MANGLER_ANNEN_OMSORGSYTER -> OpptjeningUtfall.MANGLER_INFORMASJON_OM_ANNEN_OMSORGSYTER
    }
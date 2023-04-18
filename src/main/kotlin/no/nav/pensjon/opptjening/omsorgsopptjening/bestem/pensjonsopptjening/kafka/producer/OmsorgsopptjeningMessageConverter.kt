package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Landstilknytning as KafkaLandstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidPeriode as KafkaOmsorgsarbeidPerioder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidSak as KafkaOmsorgsarbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening as KafkaOmsorgsOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Kilde as KafkaKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidSnapshot as KafkaOmsorgsarbeidsSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype as KafkaOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Utfall as KafkaUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person as KafkaPerson


fun OmsorgsOpptjening.createKafkaKey(): String = OmsorgsOpptjeningKey(
    omsorgsAr = omsorgsAr,
    omsorgsyter = omsorgsyter.gjeldendeFnr.fnr,
    utfall = mapToKafkaUtfall(utfall)
).mapToJson()

fun OmsorgsOpptjening.createKafkaValue(): String = KafkaOmsorgsOpptjening(
    omsorgsAr = omsorgsAr,
    omsorgsyter = KafkaPerson(omsorgsyter.gjeldendeFnr.fnr),
    omsorgsmottakereInvilget = omsorgsmottakereInvilget.map { KafkaPerson(it.gjeldendeFnr.fnr) },
    grunnlag = map(omsorgsarbeidSnapshot),
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

fun map(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot): KafkaOmsorgsarbeidsSnapshot {
    return KafkaOmsorgsarbeidsSnapshot(
        omsorgsyter = convertPerson(omsorgsarbeidSnapshot.omsorgsyter),
        omsorgsAr = omsorgsarbeidSnapshot.omsorgsAr,
        omsorgstype = convertToOmsorgsarbeidsType(omsorgsarbeidSnapshot.omsorgstype),
        kjoreHash = omsorgsarbeidSnapshot.kjoreHashe,
        kilde = convertToOmsorgsarbeidsKilde(omsorgsarbeidSnapshot.kilde),
        omsorgsarbeidSaker = omsorgsarbeidSnapshot.omsorgsarbeidSaker.map { sak ->
            KafkaOmsorgsarbeidSak(
                omsorgsarbeidPerioder = sak.omsorgsarbeidPerioder.map { periode ->
                    KafkaOmsorgsarbeidPerioder(
                        fom = periode.fom,
                        tom = periode.tom,
                        prosent = periode.prosent,
                        omsorgsytere = periode.omsorgsytere.map { convertPerson(it) }.toSet(),
                        omsorgsmottakere = periode.omsorgsmottakere.map { convertPerson(it) }.toSet(),
                        landstilknytning = convertLandstilknytning(periode.landstilknytning)
                    )
                }
            )
        }
    )
}

private fun convertPerson(person: Person) =
    KafkaPerson(
        fnr = person.gjeldendeFnr.fnr,
        fodselsAr = person.fodselsAr.toString()
    )

private fun convertToOmsorgsarbeidsType(omsorgsType: Omsorgstype) = when (omsorgsType) {
    Omsorgstype.BARNETRYGD -> KafkaOmsorgstype.BARNETRYGD
    Omsorgstype.HJELPESTONAD_SATS_3 -> KafkaOmsorgstype.HJELPESTØNAD_SATS_3
    Omsorgstype.HJELPESTONAD_SATS_4 -> KafkaOmsorgstype.HJELPESTØNAD_SATS_4
}

private fun convertToOmsorgsarbeidsKilde(kilde: Kilde) = when (kilde) {
    Kilde.BARNETRYGD -> KafkaKilde.BARNETRYGD
    Kilde.INFOTRYGD -> KafkaKilde.INFOTRYGD
}

private fun convertLandstilknytning(tilknytning: Landstilknytning) = when (tilknytning) {
    Landstilknytning.EOS -> KafkaLandstilknytning.EØS
    Landstilknytning.NASJONAL -> KafkaLandstilknytning.NASJONAL
}
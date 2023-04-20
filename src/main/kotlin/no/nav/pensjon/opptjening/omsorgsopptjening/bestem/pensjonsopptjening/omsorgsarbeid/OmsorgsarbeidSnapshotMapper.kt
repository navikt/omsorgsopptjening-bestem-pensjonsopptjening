package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Landstilknytning as KafkaLandstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Kilde as KafkaKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag as KafkaOmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype as KafkaOmsorgstype

class OmsorgsarbeidSnapshotMapper {
    companion object {
        fun map(omsorgsarbeidsSnapshot: KafkaOmsorgsGrunnlag, persistertePersoner: List<Person>) =
            OmsorgsGrunnlag(
                omsorgsAr = omsorgsarbeidsSnapshot.omsorgsAr,
                omsorgsyter = persistertePersoner.hentPerson(omsorgsarbeidsSnapshot.omsorgsyter.fnr),
                omsorgstype = convertToOmsorgstype(omsorgsarbeidsSnapshot.omsorgstype),
                kilde = convertToKilde(omsorgsarbeidsSnapshot.kilde),
                kjoreHashe = omsorgsarbeidsSnapshot.kjoreHash,
                omsorgsSaker = omsorgsarbeidsSnapshot.omsorgsSaker.map { sak ->
                    OmsorgsSak(
                        omsorgsvedtakPerioder = sak.omsorgVedtakPeriode.map { periode ->
                            OmsorgsvedtakPeriode(
                                fom = periode.fom,
                                tom = periode.tom,
                                prosent = periode.prosent,
                                omsorgsytere = periode.omsorgsytere.map { persistertePersoner.hentPerson(it.fnr) },
                                omsorgsmottakere = periode.omsorgsmottakere.map { persistertePersoner.hentPerson(it.fnr) },
                                landstilknytning = convertLandstilknytning(periode.landstilknytning)
                            )
                        }
                    )
                },
            )


        private fun convertToOmsorgstype(omsorgsarbeidsType: KafkaOmsorgstype) = when (omsorgsarbeidsType) {
            KafkaOmsorgstype.BARNETRYGD -> Omsorgstype.BARNETRYGD
            KafkaOmsorgstype.HJELPESTØNAD_SATS_3 -> Omsorgstype.HJELPESTONAD_SATS_3
            KafkaOmsorgstype.HJELPESTØNAD_SATS_4 -> Omsorgstype.HJELPESTONAD_SATS_4
        }

        private fun convertToKilde(omsorgsarbeidsKilde: KafkaKilde) = when (omsorgsarbeidsKilde) {
            KafkaKilde.BARNETRYGD -> Kilde.BARNETRYGD
            KafkaKilde.INFOTRYGD -> Kilde.INFOTRYGD
        }

        private fun List<Person>.hentPerson(fnr: String) = first { it.identifiseresAv(fnr) }


        private fun convertLandstilknytning(tilknytning: KafkaLandstilknytning) =
            when (tilknytning) {
                KafkaLandstilknytning.EØS -> Landstilknytning.EOS
                KafkaLandstilknytning.NASJONAL -> Landstilknytning.NASJONAL
            }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Landstilknytning as KafkaLandstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeid as KafkaOmsorgsarbeidPerioder
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsArbeidSak as KafkaOmsorgsarbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsKilde as KafkaKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot as KafkaOmsorgsarbeidsSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsType as KafkaOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person as KafkaPerson

class OmsorgsarbeidSnapshotMapper {
    companion object {
        fun map(omsorgsarbeidsSnapshot: KafkaOmsorgsarbeidsSnapshot, persistertePersoner: List<Person>) =
            OmsorgsarbeidSnapshot(
                omsorgsAr = omsorgsarbeidsSnapshot.omsorgsAr,
                omsorgsyter = persistertePersoner.hentPerson(omsorgsarbeidsSnapshot.omsorgsyter.fnr),
                omsorgstype = convertToOmsorgstype(omsorgsarbeidsSnapshot.omsorgstype),
                kilde = convertToKilde(omsorgsarbeidsSnapshot.kilde),
                kjoreHashe = omsorgsarbeidsSnapshot.kjoreHash,
                omsorgsarbeidSaker = omsorgsarbeidsSnapshot.omsorgsarbeidSaker.map { sak ->
                    OmsorgsarbeidSak(
                        omsorgsarbeidPerioder = sak.omsorgsarbeidPerioder.map { arbeid ->
                            OmsorgsarbeidPeriode(
                                fom = arbeid.fom,
                                tom = arbeid.tom,
                                prosent = arbeid.prosent,
                                omsorgsytere = arbeid.omsorgsytere.map { persistertePersoner.hentPerson(it.fnr) },
                                omsorgsmottakere = arbeid.omsorgsmottakere.map { persistertePersoner.hentPerson(it.fnr) },
                                landstilknytning = convertLandstilknytning(arbeid.landstilknytning)
                            )
                        }
                    )
                },
            )

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

        private fun convertPerson(person: Person) = KafkaPerson(
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

        private fun convertLandstilknytning(tilknytning : Landstilknytning) =
            when(tilknytning){
                Landstilknytning.EOS -> KafkaLandstilknytning.EØS
                Landstilknytning.NASJONAL -> KafkaLandstilknytning.NASJONAL
            }

        private fun convertLandstilknytning(tilknytning : KafkaLandstilknytning) =
            when(tilknytning){
                KafkaLandstilknytning.EØS -> Landstilknytning.EOS
                KafkaLandstilknytning.NASJONAL -> Landstilknytning.NASJONAL
            }
    }
}
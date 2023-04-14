package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*

class OmsorgsarbeidSnapshotMapper {
    companion object {
        fun map(omsorgsarbeidsSnapshot: OmsorgsarbeidsSnapshot, persistertePersoner: List<Person>) =
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
                            )
                        }
                    )
                },
            )

        fun map(omsorgsarbeidSnapshot: OmsorgsarbeidSnapshot): OmsorgsarbeidsSnapshot {
            return OmsorgsarbeidsSnapshot(
                omsorgsyter = convertPerson(omsorgsarbeidSnapshot.omsorgsyter),
                omsorgsAr = omsorgsarbeidSnapshot.omsorgsAr,
                omsorgstype = convertToOmsorgsarbeidsType(omsorgsarbeidSnapshot.omsorgstype),
                kjoreHash = omsorgsarbeidSnapshot.kjoreHashe,
                kilde = convertToOmsorgsarbeidsKilde(omsorgsarbeidSnapshot.kilde),
                omsorgsarbeidSaker = omsorgsarbeidSnapshot.omsorgsarbeidSaker.map { sak ->
                    OmsorgsArbeidSak(
                        omsorgsarbeidPerioder = sak.omsorgsarbeidPerioder.map { periode ->
                            OmsorgsArbeid(
                                fom = periode.fom,
                                tom = periode.tom,
                                prosent = periode.prosent,
                                omsorgsytere = periode.omsorgsytere.map { convertPerson(it) }.toSet(),
                                omsorgsmottakere = periode.omsorgsmottakere.map { convertPerson(it) }.toSet(),
                            )
                        }
                    )
                }
            )
        }


        private fun convertToOmsorgstype(omsorgsarbeidsType: OmsorgsarbeidsType) = when (omsorgsarbeidsType) {
            OmsorgsarbeidsType.BARNETRYGD -> Omsorgstype.BARNETRYGD
            OmsorgsarbeidsType.HJELPESTØNAD_SATS_3 -> Omsorgstype.HJELPESTONAD_SATS_3
            OmsorgsarbeidsType.HJELPESTØNAD_SATS_4 -> Omsorgstype.HJELPESTONAD_SATS_4
        }

        private fun convertToKilde(omsorgsarbeidsKilde: OmsorgsarbeidsKilde) = when (omsorgsarbeidsKilde) {
            OmsorgsarbeidsKilde.BARNETRYGD -> Kilde.BARNETRYGD
            OmsorgsarbeidsKilde.INFOTRYGD -> Kilde.INFOTRYGD
        }

        private fun List<Person>.hentPerson(fnr: String) = first { it.identifiseresAv(fnr) }

        private fun convertPerson(person: Person) = Person(
            fnr = person.gjeldendeFnr.fnr,
            fodselsAr = person.fodselsAr.toString()
        )

        private fun convertToOmsorgsarbeidsType(omsorgsType: Omsorgstype) = when (omsorgsType) {
            Omsorgstype.BARNETRYGD -> OmsorgsarbeidsType.BARNETRYGD
            Omsorgstype.HJELPESTONAD_SATS_3 -> OmsorgsarbeidsType.HJELPESTØNAD_SATS_3
            Omsorgstype.HJELPESTONAD_SATS_4 -> OmsorgsarbeidsType.HJELPESTØNAD_SATS_4
        }

        private fun convertToOmsorgsarbeidsKilde(kilde: Kilde) = when (kilde) {
            Kilde.BARNETRYGD -> OmsorgsarbeidsKilde.BARNETRYGD
            Kilde.INFOTRYGD -> OmsorgsarbeidsKilde.INFOTRYGD
        }
    }
}
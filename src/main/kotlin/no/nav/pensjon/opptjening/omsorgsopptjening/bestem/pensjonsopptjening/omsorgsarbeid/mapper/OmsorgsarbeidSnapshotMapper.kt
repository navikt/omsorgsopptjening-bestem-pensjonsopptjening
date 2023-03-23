package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsType

class OmsorgsarbeidSnapshotMapper {
    companion object {
        fun map(omsorgsarbeidsSnapshot: OmsorgsarbeidsSnapshot, persistertePersoner: List<Person>) =
            OmsorgsarbeidSnapshot(
                omsorgsAr = omsorgsarbeidsSnapshot.omsorgsAr,
                omsorgsyter = persistertePersoner.hentPerson(omsorgsarbeidsSnapshot.omsorgsyter.fnr),
                omsorgstype = convertToOmsorgstype(omsorgsarbeidsSnapshot.omsorgstype),
                kilde = convertToKilde(omsorgsarbeidsSnapshot.kilde),
                kjoreHashe = omsorgsarbeidsSnapshot.kjoreHash,
                omsorgsarbeidSaker = omsorgsarbeidsSnapshot.omsorgsArbeidSaker.map { sak ->
                    OmsorgsarbeidSak(
                        omsorgsarbeidPerioder = sak.omsorgsarbedUtfort.map { arbeid ->
                            OmsorgsarbeidPeriode(
                                fom = arbeid.fom,
                                tom = arbeid.tom,
                                prosent = arbeid.prosent,
                                omsorgsyter = persistertePersoner.hentPerson(arbeid.omsorgsyter.fnr),
                                omsorgsmottakere = arbeid.omsorgsmottaker.map { persistertePersoner.hentPerson(it.fnr) },
                            )
                        }
                    )
                },
            )


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
    }
}
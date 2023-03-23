package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper.OmsorgsarbeidSnapshotMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsType
import org.springframework.stereotype.Service

@Service
class OmsorgsArbeidService(private val personService: PersonService) {

    fun getOmsorgsarbeidSnapshot(omsorgsarbeidsSnapshot: OmsorgsarbeidsSnapshot): OmsorgsarbeidSnapshot {
        val persistertePersoner = personService.getPersoner(omsorgsarbeidsSnapshot.hentPersoner().map { it.fnr })
        return OmsorgsarbeidSnapshotMapper.map(omsorgsarbeidsSnapshot, persistertePersoner)
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper.OmsorgsarbeidSnapshotMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.springframework.stereotype.Service

@Service
class OmsorgsArbeidService(private val personService: PersonService, private val omsorgsarbeidSnapshotRepository: OmsorgsarbeidSnapshotRepository) {

    fun getOmsorgsarbeidSnapshot(omsorgsarbeidsSnapshot: OmsorgsarbeidsSnapshot): OmsorgsarbeidSnapshot {
        val persistertePersoner = personService.getPersoner(omsorgsarbeidsSnapshot.hentPersoner().map { it.fnr })
        val omsorgsArbeidSnapshotEntity = OmsorgsarbeidSnapshotMapper.map(omsorgsarbeidsSnapshot, persistertePersoner)
        omsorgsarbeidSnapshotRepository.save(omsorgsArbeidSnapshotEntity)
        return omsorgsArbeidSnapshotEntity
    }
}
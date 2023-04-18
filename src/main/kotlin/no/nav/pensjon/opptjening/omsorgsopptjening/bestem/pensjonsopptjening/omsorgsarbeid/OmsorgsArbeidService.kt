package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidSnapshot as KafkaSnapshot
import org.springframework.stereotype.Service

@Service
class OmsorgsArbeidService(
    private val personService: PersonService,
    private val omsorgsarbeidSnapshotRepository: OmsorgsarbeidSnapshotRepository
) {
    fun relaterteSnapshot(snapshot: OmsorgsarbeidSnapshot) = snapshot
        .getAndreOmsorgsytere()
        .flatMap { omsorgsarbeidSnapshotRepository.find(it, snapshot.omsorgsAr) }

    fun createAndSaveOmsorgasbeidsSnapshot(omsorgsarbeidsSnapshot: KafkaSnapshot): OmsorgsarbeidSnapshot {
        val personer = personService.createPersoner(hentFnr(omsorgsarbeidsSnapshot))

        return omsorgsarbeidSnapshotRepository.save(mapKafkaMessageToDomain(omsorgsarbeidsSnapshot, personer))
    }

    private fun hentFnr(omsorgsarbeidsSnapshot: KafkaSnapshot) = omsorgsarbeidsSnapshot.hentPersoner().map { it.fnr }

    private fun mapKafkaMessageToDomain(omsorgsarbeidsSnapshot: KafkaSnapshot, personer: List<Person>) = OmsorgsarbeidSnapshotMapper.map(omsorgsarbeidsSnapshot, personer)
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.springframework.stereotype.Service
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag as KafkaSnapshot

@Service
class OmsorgsArbeidService(
    private val personService: PersonService,
    private val omsorgsarbeidSnapshotRepository: OmsorgsarbeidSnapshotRepository
) {
    fun relaterteSnapshot(snapshot: OmsorgsGrunnlag) = snapshot
        .getAndreOmsorgsytere()
        .flatMap { omsorgsarbeidSnapshotRepository.find(it, snapshot.omsorgsAr) }

//    fun createAndSaveOmsorgasbeidsSnapshot(omsorgsarbeidsSnapshot: KafkaSnapshot): OmsorgsGrunnlag {
//        val personer = personService.createPersoner(hentFnr(omsorgsarbeidsSnapshot))
//
//        return omsorgsarbeidSnapshotRepository.save(mapKafkaMessageToDomain(omsorgsarbeidsSnapshot, personer)
//          )
//    }

    private fun hentFnr(omsorgsarbeidsSnapshot: KafkaSnapshot) = omsorgsarbeidsSnapshot.hentPersoner().map { it.fnr }

    private fun mapKafkaMessageToDomain(omsorgsarbeidsSnapshot: KafkaSnapshot, personer: List<Person>) = OmsorgsarbeidSnapshotMapper.map(omsorgsarbeidsSnapshot, personer)
}
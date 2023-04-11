package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid


import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.mapper.OmsorgsarbeidSnapshotMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidSnapshot
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsarbeidsSnapshot
import org.springframework.stereotype.Service

@Service
class OmsorgsArbeidService(
    private val personService: PersonService,
    private val repository: OmsorgsarbeidSnapshotRepository
) {

    fun createAndSaveOmsorgasbeidsSnapshot(omsorgsarbeidsSnapshot: OmsorgsarbeidsSnapshot): OmsorgsarbeidSnapshot {
        val persistertePersoner = personService.createPersoner(omsorgsarbeidsSnapshot.hentPersoner().map { it.fnr })
        val omsorgsArbeidSnapshotEntity = OmsorgsarbeidSnapshotMapper.map(omsorgsarbeidsSnapshot, persistertePersoner)

        return repository.save(omsorgsArbeidSnapshotEntity)
    }

    fun relaterteSnapshot(snapshot: OmsorgsarbeidSnapshot) = snapshot
        .getAndreOmsorgsytere()
        .flatMap { repository.find(it, snapshot.omsorgsAr) }
}
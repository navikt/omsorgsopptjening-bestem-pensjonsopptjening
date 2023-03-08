package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

import jakarta.transaction.Transactional
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Component
class PersonRepository(
    val personJpaRepository: PersonJpaRepository,
    private val fnrRepository: FnrRepository
) {
    @Transactional
    fun updatePerson(pdlPerson: PdlPerson): Person {
        val person = findPerson(pdlPerson) ?: Person()
        val initialFnrs = person.alleFnr.toSet()

        val updatedPerson = person.apply {
            fodselsAr = pdlPerson.fodselsAr
            oppdaterGjeldendeFnr(pdlPerson.gjeldendeFnr)
            oppdaterHistoriskeFnr(pdlPerson.historiskeFnr)
        }

        fnrRepository.deleteByFnrIn(initialFnrs.filter { !updatedPerson.identifiseresAv(it) })

        return personJpaRepository.save(updatedPerson)
    }

    fun findPerson(pdlPerson: PdlPerson): Person? {
        val eksisterendeFnr = fnrRepository.findByFnrIn(pdlPerson.historiskeFnr + pdlPerson.gjeldendeFnr)
        checkFnrOnlyRelatedToOnePerson(eksisterendeFnr)
        return eksisterendeFnr.firstOrNull()?.person
    }

    private fun checkFnrOnlyRelatedToOnePerson(fnrs: List<Fnr?>) {
        val persons = fnrs.filterNotNull().map { it.person }.toSet()
        if (persons.size > 1) {
            SECURE_LOG.error("Multiple persons identified by fnrs: $fnrs . Person id: ${persons.map { it?.id }}")
            throw DatabaseError("Multiple persons identified by fnrs. For more information see secure log")
        }
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}

@Repository
interface PersonJpaRepository : JpaRepository<Person, Long>

class DatabaseError(message: String) : RuntimeException(message)

package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

import jakarta.transaction.Transactional
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Component
class PersonRepository(
    private val personJpaRepository: PersonJpaRepository,
    private val fnrRepository: FnrRepository
) {
    @Transactional
    fun updatePerson(pdlPerson: PdlPerson): Person {
        val persistedPerson = findPerson(pdlPerson)

        return if (persistedPerson == null) {
            personJpaRepository.save(
                Person(fodselsAr = pdlPerson.fodselsAr).apply {
                    oppdaterFnr(pdlPerson.alleFnr)
                }
            )
        } else {
            fnrRepository.deleteFnrNotInPdl(persistedPerson, pdlPerson)
            persistedPerson.apply {
                if (fodselsAr != pdlPerson.fodselsAr) fodselsAr = pdlPerson.fodselsAr
                oppdaterFnr(pdlPerson.alleFnr)
            }
        }
    }

    private fun findPerson(pdlPerson: PdlPerson): Person? {
        val personer = personJpaRepository.findByAlleFnr_FnrIn(pdlPerson.alleFnr.map { it.fnr })
        if (personer.size > 1) {
            SECURE_LOG.error("Multiple persons identified by fnrs: ${pdlPerson.historiskeFnr + pdlPerson.gjeldendeFnr} . Person id: ${personer.map { it.id }}")
            throw DatabaseError("Multiple persons identified by fnrs. For more information see secure log")
        }
        return personer.firstOrNull()
    }

    fun findPersonByFnr(fnr: String) = personJpaRepository.findByAlleFnr_FnrIn(listOf(fnr)).firstOrNull()

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }
}

@Repository
interface PersonJpaRepository : JpaRepository<Person, Long> {

    fun findByAlleFnr_FnrIn(personer: List<String>): List<Person>
}

class DatabaseError(message: String) : RuntimeException(message)

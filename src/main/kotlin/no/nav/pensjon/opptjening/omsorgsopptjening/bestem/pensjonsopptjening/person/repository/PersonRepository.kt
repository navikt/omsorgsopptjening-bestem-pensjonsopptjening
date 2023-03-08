package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

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
    val fnrRepository: FnrRepository
){
    fun updatePerson(pdlPerson:PdlPerson): Person {
        validerPerson(pdlPerson)

        val personIDb = findPerson(pdlPerson)

        return if(personIDb == null) {
            opprettPersonIdb(pdlPerson)
        } else {
            personIDb.oppdaterGjeldendeFnr(pdlPerson.gjeldendeFnr)
            personIDb.oppdaterHistoriskeFnr(pdlPerson.historiskeFnr)
            personJpaRepository.saveAndFlush(personIDb)
        }
    }

    private fun opprettPersonIdb(pdlPerson: PdlPerson): Person {
        val person = Person()
        val gjeldendeFnr = Fnr(fnr = pdlPerson.gjeldendeFnr, gjeldende = true, person = person)
        val historiskeFnr = pdlPerson.historiskeFnr.map { Fnr(fnr = it, gjeldende = false, person = person)}

        val alleFnr = mutableSetOf<Fnr>().apply {
            add(gjeldendeFnr)
            addAll(historiskeFnr)
        }
        person.alleFnr = alleFnr
        person.fodselsAr = pdlPerson.fodselsAr

        return personJpaRepository.save(person)
    }



    /**
     * Forsøker å slå opp person ved å slå opp personen i lokal DB med gjeldende fnr i PDL.
     * Deretter forsøkes det å så opp person basert på hvert historiske fnr i PDL
     */
    fun findPerson(pdlPerson: PdlPerson): Person? {
        val personMedGjeldendeFnr = fnrRepository.findPersonByFnr(pdlPerson.gjeldendeFnr)
        if(personMedGjeldendeFnr == null) {
            pdlPerson.historiskeFnr.forEach {
                val personMedHistoriskFnr = fnrRepository.findPersonByFnr(it)
                if( personMedHistoriskFnr != null) {
                    return personMedHistoriskFnr
                }
            }
        } else return personMedGjeldendeFnr
        return null
    }

    fun validerPerson(pdlPerson: PdlPerson) {
        checkFnrOnlyRelatedToOnePerson(
            fnrRepository.findByFnrIn(pdlPerson.historiskeFnr + pdlPerson.gjeldendeFnr)
        )
    }

    private fun checkFnrOnlyRelatedToOnePerson(fnrs : List<Fnr?>){
        val persons = fnrs.filterNotNull().map { it.person }.toSet()
        if(persons.size > 1){
            SECURE_LOG.error("Multiple persons identified by fnrs: $fnrs . Person id: ${persons.map{it?.id}}")
            throw DatabaseError("Multiple persons identified by fnrs. For more information see secure log")
        }
    }

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

}

@Repository
interface PersonJpaRepository : JpaRepository<Person,Long>

class DatabaseError(message: String) : RuntimeException(message)

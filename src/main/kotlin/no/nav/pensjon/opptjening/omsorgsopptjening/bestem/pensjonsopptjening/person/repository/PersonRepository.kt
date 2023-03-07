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
            personJpaRepository.save(personIDb)
        }
    }

    private fun opprettPersonIdb(pdlPerson: PdlPerson): Person {
        val gjeldendeFnr = Fnr(fnr = pdlPerson.gjeldendeFnr, gjeldende = true)
        val historiskeFnr = pdlPerson.historiskeFnr.map { Fnr(fnr = it, gjeldende = false)}

        val alleFnr = mutableSetOf<Fnr>().apply {
            add(gjeldendeFnr)
            addAll(historiskeFnr)
        }
        return personJpaRepository.save((Person(alleFnr = alleFnr, fodselsAr = pdlPerson.fodselsAr)))
    }

    fun validerPerson(pdlPerson: PdlPerson) {
        val historisk = fnrRepository.findByFnrIn(pdlPerson.historiskeFnr)
        val gjeldende = fnrRepository.findByFnr(pdlPerson.gjeldendeFnr)
        checkFnrOnlyRelatedToOnePerson(historisk + gjeldende)
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

    private fun checkFnrOnlyRelatedToOnePerson(fnrs : List<Fnr?>){
        val persons = fnrs.filterNotNull().map { it.person }.toSet()
        if(persons.size > 1){
            SECURE_LOG.error("Multiple persons identified by fnrs: $fnrs . Person id: ${persons.map{it?.id}}")
            throw DatabaseError("Multiple persons identified by fnrs. For more information see secure log")
        }
    }

    /*
    fun updateFnr(pdlPerson: PdlPerson){
        val historisk = jpaRepository.findByFnrIn(pdlPerson.historiskeFnr)
        val gjeldende = jpaRepository.findByFnr(pdlPerson.gjeldendeFnr).firstOrNull()


    }

    private fun updateGjeldende(gjeldendeFnr: String): Fnr {
        return jpaRepository.save(
            (jpaRepository.findByFnr(gjeldendeFnr).firstOrNull() ?: Fnr(fnr = gjeldendeFnr)).apply {
                gjeldende = true
            }
        )
    }

    private fun updateHistoriskeFnr(historiskeFnr: List<String>) {

        historisk.map { it.person }.

        historisk.forEach { it.gjeldende = false }
    }

     */

    companion object {
        private val SECURE_LOG = LoggerFactory.getLogger("secure")
    }

}

@Repository
interface PersonJpaRepository : JpaRepository<Person,Long>

class DatabaseError(message: String) : RuntimeException(message)

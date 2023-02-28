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
    fun updatePerson(pdlPerson:PdlPerson) {
        val historisk = fnrRepository.findByFnrIn(pdlPerson.historiskeFnr)
        val gjeldende = fnrRepository.findByFnr(pdlPerson.gjeldendeFnr)

        checkFnrOnlyRelatedToOnePerson(historisk + gjeldende)

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

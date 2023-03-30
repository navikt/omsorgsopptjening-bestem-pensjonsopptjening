package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class FnrRepository(private val jpaRepository: FnrJpaRepository) {

    fun deleteFnrNotInPdl(person: Person, pdlPerson:PdlPerson) = jpaRepository.deleteByPerson_IdAndFnrNotIn(person.id!!, pdlPerson.alleFnr.map { it.fnr })

}


@Repository
interface FnrJpaRepository : JpaRepository<Fnr, Long> {

    fun deleteByPerson_IdAndFnrNotIn(personId:Long, fnrs: List<String>): List<Fnr>

}

package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository


@Component
class PersonRepository(
    val personJpaRepository: PersonJpaRepository,
    val fnrRepository: FnrRepository
){
    fun updatePerson(person:Person){

    }

}

@Repository
interface PersonJpaRepository : JpaRepository<Person,Long>
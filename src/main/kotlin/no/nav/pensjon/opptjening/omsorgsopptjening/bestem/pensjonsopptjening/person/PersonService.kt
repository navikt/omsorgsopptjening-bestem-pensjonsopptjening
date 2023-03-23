package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val pdlService: PdlService
) {

    fun getPerson(fnr: String): Person {
        // Henter person fra PDL
        val pdlPerson = pdlService.hentPerson(fnr)


        return personRepository.updatePerson(pdlPerson)
    }

    fun getPersoner(fnrs: List<String>): List<Person> = fnrs
        .map { pdlService.hentPerson(it) }
        .distinctBy { it.gjeldendeFnr }
        .map { personRepository.updatePerson(it) }
}
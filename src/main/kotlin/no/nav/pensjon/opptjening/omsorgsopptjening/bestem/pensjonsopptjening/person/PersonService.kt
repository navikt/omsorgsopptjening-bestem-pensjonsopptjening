package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.FnrRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val fnrRepository: FnrRepository,
    private val personRepository: PersonRepository,
    private val pdlService: PdlService
) {

    fun getPerson(fnr: String): Person {
        val pdlPerson = pdlService.hentPerson(fnr)
        return Person(
            alleFnr = ((pdlPerson.historiskeFnr - pdlPerson.gjeldendeFnr).map { Fnr(fnr = it) } +  Fnr(fnr = pdlPerson.gjeldendeFnr , gjeldende = true)).toSet(),
            fodselsAr = pdlPerson.fodselsAr
        )
        /*
        val pdlPerson = pdlService.hentPerson(fnr)
        return personRepository.updatePerson(pdlPerson)

         */
    }
}
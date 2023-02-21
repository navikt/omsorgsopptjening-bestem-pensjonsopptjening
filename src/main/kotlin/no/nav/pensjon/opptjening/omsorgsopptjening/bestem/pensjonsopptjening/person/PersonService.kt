package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import org.springframework.stereotype.Service

@Service
class PersonService(private val pdlService: PdlService) {

    fun getPerson(fnr: String ): Person{
        return pdlService.hentPerson(fnr)
    }
}
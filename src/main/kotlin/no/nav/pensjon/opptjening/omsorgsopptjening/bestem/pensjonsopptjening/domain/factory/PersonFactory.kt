package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class PersonFactory {
    companion object {
        fun createPerson(gjeldendeFnr: String, historiskeFnr: List<String> = listOf()) =
            Person(
                gjeldendeFnr = Fnr(gjeldendeFnr),
                historiskeFnr = historiskeFnr.map { Fnr(it) }.toSet()
            )
    }
}
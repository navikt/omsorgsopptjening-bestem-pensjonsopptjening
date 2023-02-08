package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.factory

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person

class PersonFactory {
    companion object {
        fun createPerson(gjeldendeFnr: String, historiskeFnr: List<String> = listOf()) =
            Person(
                gjeldendeFnr = Fnr(gjeldendeFnr),
                historiskeFnr = historiskeFnr.map { Fnr(it) }.toSet()
            )
    }
}
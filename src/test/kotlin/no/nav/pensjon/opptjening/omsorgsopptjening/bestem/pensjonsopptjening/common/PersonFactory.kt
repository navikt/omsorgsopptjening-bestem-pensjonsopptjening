package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Fnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person

class PersonFactory {
    companion object {
        fun createPerson(gjeldendeFnr: String, fodselsAr: Int, historiskeFnr: List<String> = listOf()) =
            Person(
                alleFnr = historiskeFnr.map { Fnr(fnr = it) }.toSet() + Fnr(fnr = gjeldendeFnr, gjeldende = true),
                fodselsAr = fodselsAr
            )
    }
}
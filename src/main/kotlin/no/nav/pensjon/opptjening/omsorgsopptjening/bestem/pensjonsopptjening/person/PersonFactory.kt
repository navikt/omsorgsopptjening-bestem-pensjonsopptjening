package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

class PersonFactory {
    companion object {
        fun createPerson(gjeldendeFnr: String, historiskeFnr: List<String> = listOf()) =
            Person(
                gjeldendeFnr = Fnr(gjeldendeFnr),
                historiskeFnr = historiskeFnr.map { Fnr(it) }.toSet()
            )
    }
}
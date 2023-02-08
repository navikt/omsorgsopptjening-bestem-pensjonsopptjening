package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

class Person(gjeldendeFnr: Fnr, historiskeFnr: Set<Fnr> = setOf()) {

    private val alleFnr = historiskeFnr + gjeldendeFnr

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)

}
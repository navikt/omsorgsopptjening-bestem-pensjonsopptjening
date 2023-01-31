package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

class Person(gjeldendeFnr: Fnr, historiskeFnr: Set<Fnr> = setOf()) {

    private val allFnrs = historiskeFnr + gjeldendeFnr

    infix fun isSamePerson(otherPerson: Person) = (otherPerson.allFnrs intersect allFnrs).isNotEmpty()

    infix fun isIdentifiedBy(fnr: Fnr) = allFnrs.contains(fnr)

}
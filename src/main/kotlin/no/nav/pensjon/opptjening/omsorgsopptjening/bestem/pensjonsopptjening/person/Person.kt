package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

class Person(
    val gjeldendeFnr: Fnr,
    val historiskeFnr: Set<Fnr> = setOf(),
    val fodselsAr: Int
) {

    val alleFnr = historiskeFnr + gjeldendeFnr

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)
}
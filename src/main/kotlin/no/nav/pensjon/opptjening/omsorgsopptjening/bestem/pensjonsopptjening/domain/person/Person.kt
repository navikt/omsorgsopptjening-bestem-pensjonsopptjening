package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

class Person(
    private val gjeldendeFnr: Fnr,
    historiskeFnr: Set<Fnr> = setOf()
) {

    private val allFnrs = historiskeFnr + gjeldendeFnr

    infix fun isIdentifiedBy(fnr: Fnr) = allFnrs.contains(fnr)

}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

data class Person(
    private val gjeldendeFnr: Fnr,
    private val historiskeFnr: List<Fnr> = listOf()
) {
    infix fun isIdentifiedBy(fnr: Fnr) = gjeldendeFnr == fnr || historiskeFnr.contains(fnr)
}
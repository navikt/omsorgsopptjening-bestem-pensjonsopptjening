package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

data class Person(
    private val gjeldendeFnr: Fnr,
    private val historiskeFnr: List<Fnr> = listOf()
)
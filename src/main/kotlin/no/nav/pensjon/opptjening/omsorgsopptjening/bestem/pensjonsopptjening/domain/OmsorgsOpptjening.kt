package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val omsorgsarbeid: OmsorgsArbeid,
    val involvertePerson: Person
) {
    fun personMedPomsorgsopptjening() =
        if (omsorgsarbeid.monthsWithOmsorg(omsorgsAr) >= 6) involvertePerson else null

}
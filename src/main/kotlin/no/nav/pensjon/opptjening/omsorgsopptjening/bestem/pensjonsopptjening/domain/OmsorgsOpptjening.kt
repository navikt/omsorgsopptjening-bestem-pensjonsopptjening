package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val omsorgsarbeid: OmsorgsArbeidSak,
    val involvertePerson: Person
) {
    fun personMedOmsorgsopptjening() = if (omsorgsarbeid.monthsWithOmsorgsarbeid(omsorgsAr) >= 6) involvertePerson else null

}
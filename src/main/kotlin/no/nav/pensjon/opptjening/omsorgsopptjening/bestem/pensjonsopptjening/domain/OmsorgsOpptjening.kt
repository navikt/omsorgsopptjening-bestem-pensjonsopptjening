package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val omsorgsarbeidSak: OmsorgsArbeidSak,
    val involvertePerson: Person
) {
    fun personMedOmsorgsopptjening() = if (omsorgsarbeidSak.monthsWithOmsorgsarbeid(omsorgsAr, involvertePerson) >= 6) involvertePerson else null

}
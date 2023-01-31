package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val omsorgsarbeidSak: OmsorgsArbeidSak,
    val personer: List<Person>
) {
    fun personMedOmsorgsopptjening() = personer.filter { (omsorgsarbeidSak.monthsWithOmsorgsarbeid(omsorgsAr, it) >= 6) }
}
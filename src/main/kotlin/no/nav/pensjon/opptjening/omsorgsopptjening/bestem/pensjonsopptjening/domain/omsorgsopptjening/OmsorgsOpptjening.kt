package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid.OmsorgsArbeidSak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsOpptjening(
    val omsorgsAr: Int,
    val omsorgsarbeidSak: OmsorgsArbeidSak,
    val personer: List<Person>
) {
    fun personerMedOmsorgsopptjening() = personer.filter { person ->
        omsorgsarbeidSak.monthsWithOmsorgsarbeid(omsorgsAr, person) >= 6
    }

}
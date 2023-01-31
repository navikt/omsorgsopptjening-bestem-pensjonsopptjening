package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsArbeidSak internal constructor(
    private val utfortOmsorgsArbeid: List<UtfortOmsorgsarbeid>,
) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int, person: Person) =
        utfortOmsorgsArbeid
            .filter { it isUtfortAvPerson person }
            .sumOf { it.monthsWithOmsorgsarbeid(omsorgsAr) }
}

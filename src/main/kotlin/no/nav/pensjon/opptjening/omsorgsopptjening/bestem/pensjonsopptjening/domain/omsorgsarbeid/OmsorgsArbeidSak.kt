package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person.Person

class OmsorgsArbeidSak internal constructor(
    private val utfortOmsorgsArbeid: List<Omsorgsarbeid>,
) {
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int, person: Person) =
        utfortOmsorgsArbeid
            .filter { omsorgsarbeid -> omsorgsarbeid erUtfortAv person }
            .sumOf { omsorgsarbeid -> omsorgsarbeid mondederMedUtbetalinger omsorgsAr }
}

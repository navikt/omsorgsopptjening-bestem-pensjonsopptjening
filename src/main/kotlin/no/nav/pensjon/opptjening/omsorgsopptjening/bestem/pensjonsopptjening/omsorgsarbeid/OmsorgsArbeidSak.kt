package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.Grunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.GrunnlagsVisitor
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person

class OmsorgsArbeidSak internal constructor(val utfortOmsorgsArbeid: List<Omsorgsarbeid>) : Grunnlag {
    fun involvetePersoner() = utfortOmsorgsArbeid.map { it.person }
    fun monthsWithOmsorgsarbeid(omsorgsAr: Int, person: Person) =
        utfortOmsorgsArbeid
            .filter { omsorgsarbeid -> omsorgsarbeid erUtfortAv person }
            .sumOf { omsorgsarbeid -> omsorgsarbeid mondederMedUtbetalinger omsorgsAr }

    override fun accept(grunnlagsVisitor: GrunnlagsVisitor) {
        grunnlagsVisitor.visit(this)
        utfortOmsorgsArbeid.forEach { it.accept(grunnlagsVisitor) }
    }

    override fun dataObject() = OmsorgsArbeidSakDataObject(utfortOmsorgsArbeid.map { it.dataObject() })
}

data class OmsorgsArbeidSakDataObject(val utfortOmsorgsArbeid: List<OmsorgsarbeidDataObject>)
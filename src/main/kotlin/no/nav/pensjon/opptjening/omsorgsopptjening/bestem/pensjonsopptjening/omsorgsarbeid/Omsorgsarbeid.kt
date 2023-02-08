package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.Grunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.GrunnlagsVisitor
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.UtbetalingMoneder.Companion.utbetalingMoneder
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.PersonDataObject

class Omsorgsarbeid(
    private val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalinger>,
    private val person: Person
) : Grunnlag{
    infix fun mondederMedUtbetalinger(omsorgsAr: Int): Int = (mondederMedUtbetalingerTotalt begrensTilAr omsorgsAr).antall()

    infix fun erUtfortAv(annenPerson: Person) = annenPerson erSammePerson person

    fun getPerson() = person

    private val mondederMedUtbetalingerTotalt
        get() =
            omsorgsArbeidsUtbetalinger
                .map { utbetalinger -> utbetalinger.utbetalingMoneder() }
                .fold(utbetalingMoneder()) { acc, moneder -> acc + moneder }

    override fun accept(grunnlagsVisitor: GrunnlagsVisitor) {
        grunnlagsVisitor.visit(this)
        omsorgsArbeidsUtbetalinger.forEach{it.accept(grunnlagsVisitor)}
        person.accept(grunnlagsVisitor)
    }

    override fun dataObject() =   OmsorgsarbeidDataObject(omsorgsArbeidsUtbetalinger.map { it.dataObject() }, person.dataObject())
}

data class OmsorgsarbeidDataObject(val omsorgsArbeidsUtbetalinger: List<OmsorgsArbeidsUtbetalingerDataObject>, val person : PersonDataObject)
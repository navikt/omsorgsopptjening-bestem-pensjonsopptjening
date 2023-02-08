package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.Grunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.GrunnlagsVisitor

class Person(private val gjeldendeFnr: Fnr, historiskeFnr: Set<Fnr> = setOf()) : Grunnlag {

    private val alleFnr = historiskeFnr + gjeldendeFnr

    infix fun erSammePerson(annenPerson: Person) = (annenPerson.alleFnr intersect alleFnr).isNotEmpty()

    infix fun identifiseresAv(fnr: Fnr) = alleFnr.contains(fnr)
    override fun accept(grunnlagsVisitor: GrunnlagsVisitor) {
        grunnlagsVisitor.visit(this)
        gjeldendeFnr.accept(grunnlagsVisitor)
    }

    override fun dataObject() = PersonDataObject(gjeldendeFnr.dataObject())
}

data class PersonDataObject(val gjeldendeFnr: FnrDataObject)
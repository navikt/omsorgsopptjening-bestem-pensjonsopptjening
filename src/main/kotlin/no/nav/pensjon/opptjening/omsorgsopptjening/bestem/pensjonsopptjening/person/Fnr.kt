package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.Grunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag.GrunnlagsVisitor

class Fnr(private val fnr: String) : Grunnlag {

    override fun equals(other: Any?) = other === this || (other is Fnr && other.fnr == fnr)

    override fun hashCode() = fnr.hashCode()

    override fun dataObject() = FnrDataObject(fnr)

    override fun accept(grunnlagsVisitor: GrunnlagsVisitor) {
        grunnlagsVisitor.visit(this)
    }
}

data class FnrDataObject(val fnr: String)
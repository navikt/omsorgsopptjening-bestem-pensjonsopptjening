package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.domain.person

class Fnr(private val fnr: String) {

    override fun equals(other: Any?) = other === this || (other is Fnr && other.fnr == fnr)

    override fun hashCode() = fnr.hashCode()
}
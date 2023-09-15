package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person

interface PersonOppslag {
    fun hentPerson(fnr: String): Person
    fun hentAktørId(fnr: String): String
}

data class PersonOppslagException(
    val msg: String,
    val throwable: Throwable
) : RuntimeException(msg, throwable)
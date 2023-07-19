package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import java.time.LocalDate


internal data class PersonDb(
    val fnr: String,
    val fødselsdato: String
)

internal fun Person.toDb(): PersonDb {
    return PersonDb(
        fnr = fnr,
        fødselsdato = fødselsdato.toString()
    )
}

internal fun PersonDb.toDomain(): Person {
    return Person(
        fnr = fnr,
        fødselsdato = LocalDate.parse(fødselsdato),
    )
}

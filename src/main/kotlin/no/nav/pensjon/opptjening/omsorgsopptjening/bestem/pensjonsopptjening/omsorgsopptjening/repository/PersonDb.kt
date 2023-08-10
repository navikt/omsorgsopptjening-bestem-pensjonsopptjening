package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("PersonDb")
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

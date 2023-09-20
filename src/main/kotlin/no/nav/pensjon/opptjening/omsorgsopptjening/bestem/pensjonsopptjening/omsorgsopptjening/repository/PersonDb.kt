package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
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
    val fødselsdato: String,
    val dødsdato: String?,
    val familierelasjoner: Map<String, String>
)

internal fun Person.toDb(): PersonDb {
    return PersonDb(
        fnr = fnr,
        fødselsdato = fødselsdato.toString(),
        dødsdato = dødsdato?.toString(),
        familierelasjoner = familierelasjoner.relasjoner
            .associate { it.ident to it.relasjon.toString() }
    )
}

internal fun PersonDb.toDomain(): Person {
    return Person(
        fnr = fnr,
        fødselsdato = LocalDate.parse(fødselsdato),
        dødsdato = dødsdato?.let { LocalDate.parse(it) },
        familierelasjoner = familierelasjoner
            .map { Familierelasjon(it.key, Familierelasjon.Relasjon.valueOf(it.value)) }
            .let { Familierelasjoner(it) }
    )
}

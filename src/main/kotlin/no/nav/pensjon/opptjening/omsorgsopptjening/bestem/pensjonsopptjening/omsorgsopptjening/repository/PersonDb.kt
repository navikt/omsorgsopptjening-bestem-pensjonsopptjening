package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ident
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.IdentHistorikk
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonTypeName("PersonDb")
internal data class PersonDb(
    val fødselsdato: String,
    val dødsdato: String?,
    val familierelasjoner: Map<String, String>,
    val identhistorikk: List<IdentDb>,
)

internal fun Person.toDb(): PersonDb {
    return PersonDb(
        fødselsdato = fødselsdato.toString(),
        dødsdato = dødsdato?.toString(),
        familierelasjoner = familierelasjoner.toDb(),
        identhistorikk = identhistorikk.toDb(),
    )
}

internal fun PersonDb.toDomain(): Person {
    return Person(
        fødselsdato = LocalDate.parse(fødselsdato),
        dødsdato = dødsdato?.let { LocalDate.parse(it) },
        familierelasjoner = familierelasjoner.toDomain(),
        identhistorikk = identhistorikk.toDomain()
    )
}

internal fun Familierelasjoner.toDb(): Map<String, String> {
    return relasjoner.associate { it.ident.ident to it.relasjon.toString() }
}

internal fun Map<String, String>.toDomain(): Familierelasjoner {
    return Familierelasjoner(map { Familierelasjon(it.key, Familierelasjon.Relasjon.valueOf(it.value)) })
}

internal fun List<IdentDb>.toDomain(): IdentHistorikk {
    return IdentHistorikk(map { it.toDomain() }.filterIsInstance<Ident.FolkeregisterIdent>().toSet())
}

internal fun IdentDb.toDomain(): Ident {
    return when {
        ident != Ident.IDENT_UKJENT && gjeldende -> {
            Ident.FolkeregisterIdent.Gjeldende(ident)
        }

        ident != Ident.IDENT_UKJENT && !gjeldende -> {
            Ident.FolkeregisterIdent.Historisk(ident)
        }

        else -> {
            Ident.Ukjent
        }
    }
}

internal fun IdentHistorikk.toDb(): List<IdentDb> {
    return historikk().map { it.toDb() }
}

internal fun Ident.toDb(): IdentDb {
    return when (this) {
        is Ident.FolkeregisterIdent.Gjeldende -> {
            IdentDb(ident = ident, gjeldende = true)
        }

        is Ident.FolkeregisterIdent.Historisk -> {
            IdentDb(ident = ident, gjeldende = false)
        }

        Ident.Ukjent -> {
            IdentDb(ident = Ident.IDENT_UKJENT, gjeldende = false)
        }
    }
}

internal data class IdentDb(
    val ident: String,
    val gjeldende: Boolean
)

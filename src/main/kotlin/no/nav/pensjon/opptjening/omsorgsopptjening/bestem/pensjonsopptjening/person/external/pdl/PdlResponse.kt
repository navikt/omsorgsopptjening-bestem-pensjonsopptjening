package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Ident
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.IdentHistorikk
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import java.time.LocalDate
import java.time.LocalDateTime

internal data class PdlResponse(
    val data: PdlData?,
    private val errors: List<PdlError>? = null,
    private val extensions: Extensions? = null,
) {
    val error: PdlError? = errors?.firstOrNull()
    val warnings: List<Extension>? = extensions?.warnings
}

internal data class Extensions(
    val warnings: List<Extension>? = null
)

internal data class Extension(
    val code: String
)

internal data class PdlData(
    val hentPerson: HentPersonQueryResponse?
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class HentPersonQueryResponse(
    val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
    val foedselsdato: List<Foedselsdato>,
    val doedsfall: List<Doedsfall?>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
) {
    private fun identhistorikk(): IdentHistorikk {
        return folkeregisteridentifikator.identhistorikk()
    }

    private fun bestemDoedsdato(doedsfall: List<Doedsfall?>): LocalDate? {
        return doedsfall.firstOrNull()?.doedsdato
    }

    private fun foedselsdato(): LocalDate {
        return when (foedselsdato.size) {
            0 -> {
                throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
            }

            1 -> {
                LocalDate.parse(foedselsdato.first().foedselsdato)
            }

            else -> {
                LocalDate.parse(foedselsdato.avklarFoedsel()?.foedselsdato)
                    ?: throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
            }
        }
    }

    private fun familierelasjoner(): Familierelasjoner {
        return forelderBarnRelasjon.map { relasjon ->
            Familierelasjon(
                ident = relasjon.relatertPersonsIdent?.let { Ident.FolkeregisterIdent.Gjeldende(it) } ?: Ident.Ukjent,
                relasjon = when (relasjon.relatertPersonsRolle) {
                    ForelderBarnRelasjon.Rolle.BARN -> Familierelasjon.Relasjon.BARN
                    ForelderBarnRelasjon.Rolle.FAR -> Familierelasjon.Relasjon.FAR
                    ForelderBarnRelasjon.Rolle.MOR -> Familierelasjon.Relasjon.MOR
                    ForelderBarnRelasjon.Rolle.MEDMOR -> Familierelasjon.Relasjon.MEDMOR
                }
            )
        }.let {
            Familierelasjoner(it)
        }
    }

    private fun List<Folkeregisteridentifikator>.identhistorikk(): IdentHistorikk {
        return IdentHistorikk(
            map {
                when (it.status) {
                    Folkeregisteridentifikator.Status.I_BRUK -> {
                        Ident.FolkeregisterIdent.Gjeldende(it.identifikasjonsnummer)
                    }

                    Folkeregisteridentifikator.Status.OPPHOERT -> {
                        Ident.FolkeregisterIdent.Historisk(it.identifikasjonsnummer)
                    }
                }
            }.toSet()
        )
    }

    fun toDomain(): Person {
        return Person(
            fødselsdato = foedselsdato(),
            dødsdato = bestemDoedsdato(doedsfall),
            familierelasjoner = familierelasjoner(),
            identhistorikk = identhistorikk(),
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Folkeregisteridentifikator(
    val identifikasjonsnummer: String,
    val status: Status,
    val type: Type,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
) {
    fun erGjeldende(): Boolean {
        return status == Status.I_BRUK
    }

    enum class Status { I_BRUK, OPPHOERT }
    enum class Type { FNR, DNR }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Foedselsdato(
    val foedselsaar: Int,
    val foedselsdato: String,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ForelderBarnRelasjon(
    val relatertPersonsIdent: String?,
    val relatertPersonsRolle: Rolle,
    val minRolleForPerson: Rolle,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
    val relatertPersonUtenFolkeregisteridentifikator: RelatertPersonUtenFolkeregisterident?
) {
    enum class Rolle {
        FAR,
        MOR,
        MEDMOR,
        BARN
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Doedsfall(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val doedsdato: LocalDate
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Metadata(
    val historisk: Boolean,
    val master: String,
    val endringer: List<Endring> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Folkeregistermetadata(
    val ajourholdstidspunkt: LocalDateTime? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Endring(
    val registrert: LocalDateTime
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class RelatertPersonUtenFolkeregisterident(
    //tar bare i mot et av feltene da vi uansett konverterer opplysningene til "ukjent" til domenet
    val foedselsdato: String?

)
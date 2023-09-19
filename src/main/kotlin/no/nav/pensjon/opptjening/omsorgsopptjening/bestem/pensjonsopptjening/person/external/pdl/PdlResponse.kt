package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Familierelasjoner
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import java.time.LocalDate
import java.time.LocalDateTime

internal data class PdlResponse(
    val data: PdlData,
    private val errors: List<PdlError>? = null
) {
    val error: PdlError? = errors?.firstOrNull()
}

internal data class PdlData(
    val hentPerson: HentPersonQueryResponse?
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class HentPersonQueryResponse(
    val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
    val foedsel: List<Foedsel>,
    val doedsfall: List<Doedsfall?>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
) {
    private fun gjeldendeIdent(): PdlFnr {
        return folkeregisteridentifikator
            .firstOrNull { it.status == Folkeregisteridentifikator.Status.I_BRUK }
            ?.let { PdlFnr(it.identifikasjonsnummer, gjeldende = true) }
            ?: throw PdlMottatDataException("Fnr i bruk finnes ikke")
    }

    private fun bestemDoedsdato(doedsfall: List<Doedsfall?>): LocalDate? {
        return doedsfall.firstOrNull()?.doedsdato
    }

    private fun foedselsdato(): LocalDate {
        return when (foedsel.size) {
            0 -> {
                throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
            }

            1 -> {
                LocalDate.parse(foedsel.first().foedselsdato)
            }

            else -> {
                LocalDate.parse(foedsel.avklarFoedsel()?.foedselsdato)
                    ?: throw PdlMottatDataException("Fødselsår finnes ikke i respons fra pdl")
            }
        }
    }

    private fun familierelasjoner(): Familierelasjoner {
        return forelderBarnRelasjon.map {
            Familierelasjon(
                ident = it.relatertPersonsIdent,
                relasjon = when (it.relatertPersonsRolle) {
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

    fun toDomain(): Person {
        return Person(
            fnr = gjeldendeIdent().fnr,
            fødselsdato = foedselsdato(),
            dødsdato = bestemDoedsdato(doedsfall),
            familierelasjoner = familierelasjoner(),
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
    enum class Status { I_BRUK, OPPHOERT }
    enum class Type { FNR, DNR }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Foedsel(
    val foedselsaar: Int,
    val foedselsdato: String,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ForelderBarnRelasjon(
    val relatertPersonsIdent: String,
    val relatertPersonsRolle: Rolle,
    val minRolleForPerson: Rolle,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
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
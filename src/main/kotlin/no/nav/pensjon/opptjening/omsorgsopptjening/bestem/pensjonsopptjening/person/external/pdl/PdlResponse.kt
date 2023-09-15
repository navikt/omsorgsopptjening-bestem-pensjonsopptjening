package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

internal data class PdlResponse(val data: PdlData, private val errors: List<PdlError>? = null) {
    val error: PdlError? = errors?.firstOrNull()
}

internal data class PdlData(val hentPerson: HentPersonQueryResponse?)

internal data class HentPersonQueryResponse(
    val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
    val foedsel: List<Foedsel>,
    val doedsfall: List<Doedsfall?>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
)

internal data class Folkeregisteridentifikator(
    val identifikasjonsnummer: String,
    val status: Status,
    val type: Type,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

internal data class Foedsel(
    val foedselsaar: Int,
    val foedselsdato: String,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

internal data class ForelderBarnRelasjon(
    val relatertPersonsIdent: String,
    val relatertPersonsRolle: String,
    val minRolleForPerson: String,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

internal data class Doedsfall(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val doedsdato: LocalDate
)

internal data class Metadata(val historisk: Boolean, val master: String, val endringer: List<Endring> = emptyList())

internal data class Folkeregistermetadata(val ajourholdstidspunkt: LocalDateTime? = null)

internal data class Endring(val registrert: LocalDateTime)

internal enum class Status { I_BRUK, OPPHOERT }

internal enum class Type { FNR, DNR }
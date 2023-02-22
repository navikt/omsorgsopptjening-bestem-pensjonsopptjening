package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import java.time.LocalDate
import java.time.LocalDateTime

data class PdlResponse(val data: PdlData, private val errors: List<PdlError>? = null) {
    val error: PdlError? = errors?.firstOrNull()
}

data class PdlData(val hentPerson: PdlPerson?)

data class PdlPerson(
    val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
    val foedsel: List<Foedsel>,
)

data class Folkeregisteridentifikator(
    val identifikasjonsnummer: String,
    val status: Status,
    val type: Type,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

data class Foedsel(
    val foedselsaar: Int,
    val metadata: Metadata,
    val folkeregistermetadata: Folkeregistermetadata? = null,
)

data class Metadata(val historisk: Boolean, val master: String, val endringer: List<Endring> = emptyList())

data class Folkeregistermetadata(val ajourholdstidspunkt: LocalDateTime? = null)

data class Endring(val registrert: LocalDateTime)

enum class Status { I_BRUK, OPPHOERT }

enum class Type { FNR, DNR }
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

import java.time.LocalDate

private val pdlResponseValidationErrorCounter = io.prometheus.client.Counter.build()
    .name("pdlResponseValidationErrorCounter")
    .labelNames("type")
    .help("Counter of pdl responses that are invalid according to their own documentation").register()

data class Person(
    private val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
    private val foedsel: List<Foedsel>
) {
    val gjeldendeIdent: String
        get() {
            if (folkeregisteridentifikator.isEmpty()) {
                throwPdlResponseValidationException("Folkeregisteridentifikator ikke funnet.", "pdl_ident_ikke_funnet")
            }
            if (folkeregisteridentifikator.none { it.status == Status.I_BRUK }) {
                throwPdlResponseValidationException(
                    "Folkeregisteridentifikator I_BRUK ikke funnet i response fra PDL.",
                    "ikke_ident_I_BRUK_i_pdl_response"
                )
            }
            return folkeregisteridentifikator.first { it.status == Status.I_BRUK }.identifikasjonsnummer
        }

    val identHistorikk: List<String>
        get() {
            val gjeldendeIdentifikasjonsnummer = gjeldendeIdent
            return folkeregisteridentifikator
                .filter { it.status == Status.OPPHOERT }
                .filterNot { it.identifikasjonsnummer == gjeldendeIdentifikasjonsnummer }
                .distinctBy { it.identifikasjonsnummer }
                .map { it.identifikasjonsnummer }
        }

    val foedselsaar: Int
        get() {
            if (foedsel.isEmpty()) {
                throwPdlResponseValidationException("Foedselsaar ikke funnet.", "ikke_foedselsaar_i_pdl_response")
            }
            return foedsel.avklareParallelleSannheterOrNull().foedselsaar
        }
}

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

data class Folkeregistermetadata(val ajourholdstidspunkt: LocalDate? = null)

data class Endring(val registrert: LocalDate)

enum class Status { I_BRUK, OPPHOERT }

enum class Type { FNR, DNR }

private fun throwPdlResponseValidationException(text: String, lable: String) {
    pdlResponseValidationErrorCounter.labels(lable).inc()
}
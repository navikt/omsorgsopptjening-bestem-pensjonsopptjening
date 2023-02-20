package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl

data class PdlError(val message: String, val extensions: Extensions = Extensions())

data class Extensions(val code: String? = null) {
    val pdlErrorCode = code?.uppercase()?.let {
        try {
            PdlErrorCode.valueOf(it)
        } catch (e: IllegalArgumentException) {
            PdlErrorCode.UNKNOWN
        }
    } ?: PdlErrorCode.UNKNOWN
}

enum class PdlErrorCode {
    NOT_FOUND,
    UNAUTHORIZED,
    SERVER_ERROR,
    UNKNOWN
}
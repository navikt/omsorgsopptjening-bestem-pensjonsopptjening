package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl

import com.fasterxml.jackson.annotation.JsonProperty

internal data class PdlError(val message: String, val extensions: ErrorExtension)

internal data class ErrorExtension(val code: PdlErrorCode)

internal enum class PdlErrorCode {
    @JsonProperty("unauthenticated") UNAUTHENTICATED,
    @JsonProperty("unauthorized") UNAUTHORIZED,
    @JsonProperty("not_found") NOT_FOUND,
    @JsonProperty("bad_request") BAD_REQUEST,
    @JsonProperty("server_error") SERVER_ERROR,
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Any.toJson() = mapAnyToJson(this)

fun mapAnyToJson(data: Any, nonempty: Boolean = false): String {
    return if (nonempty) {
        jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data)
    } else {
        return jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data)
    }
}
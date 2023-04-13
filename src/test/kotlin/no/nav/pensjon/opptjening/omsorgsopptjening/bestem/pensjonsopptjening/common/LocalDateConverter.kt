package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common

import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.ArgumentConverter
import java.time.LocalDate


internal class LocalDateConverter : ArgumentConverter {
    @Throws(ArgumentConversionException::class)
    override fun convert(source: Any?, context: ParameterContext?): Any? {
        if (source == null) return null
        require(source is String) { "The argument should be a string: $source" }
        try {
            val parts = source.split("-").map { it.toInt() }
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            return LocalDate.of(year, month, day)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to convert", e)
        }
    }
}
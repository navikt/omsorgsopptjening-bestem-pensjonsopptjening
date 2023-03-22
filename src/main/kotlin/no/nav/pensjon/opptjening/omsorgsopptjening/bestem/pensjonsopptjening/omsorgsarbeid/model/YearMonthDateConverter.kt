package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import jakarta.persistence.AttributeConverter
import java.sql.Date
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId


class YearMonthDateConverter : AttributeConverter<YearMonth?, Date?> {

    override fun convertToDatabaseColumn(attribute: YearMonth?) = attribute?.let { Date.valueOf(it.atDay(1)) }

    override fun convertToEntityAttribute(dbData: Date?) = dbData?.let { YearMonth.from(Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate()) }

}
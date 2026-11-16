package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.Endringslogg
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

data class EndringsloggTestEntry(
    val id: UUID,
    val endretTidspunkt: Instant,
    val endretAv: String,
    val kilde: Endringslogg.Kilde,
    val handling: Endringslogg.Handling,
    val status: Endringslogg.Status,
    val inputId: UUID,
    val resultatId: UUID?,
    val detalj: String?,
    val begrunnelse: String?,
    val opprettet: Instant,
)

private class EndringsloggTestMapper : RowMapper<EndringsloggTestEntry> {
    override fun mapRow(rs: ResultSet, rowNum: Int): EndringsloggTestEntry {
        return EndringsloggTestEntry(
            id = UUID.fromString(rs.getString("id")),
            endretTidspunkt = rs.getTimestamp("endret_tidspunkt").toInstant(),
            endretAv = rs.getString("endret_av"),
            kilde = Endringslogg.Kilde.fromDbValue(rs.getString("kilde")),
            handling = Endringslogg.Handling.fromDbValue(rs.getString("handling")),
            status = Endringslogg.Status.fromDbValue(rs.getString("status")),
            inputId = rs.getObject("input_id", UUID::class.java),
            resultatId = rs.getObject("resultat_id", UUID::class.java),
            detalj = rs.getString("detalj"),
            begrunnelse = rs.getString("begrunnelse"),
            opprettet = rs.getTimestamp("opprettet").toInstant(),
        )
    }
}

fun NamedParameterJdbcTemplate.finnEndringslogg(id: UUID): EndringsloggTestEntry {
    return query(
        """select * from endringslogg where id = :id""",
        mapOf("id" to id),
        EndringsloggTestMapper()
    ).single()
}

fun NamedParameterJdbcTemplate.finnEndringsloggForHandling(handling: Endringslogg.Handling): List<EndringsloggTestEntry> {
    return query(
        """select * from endringslogg
            |where handling = :handling
            |order by endret_tidspunkt desc, opprettet desc""".trimMargin(),
        mapOf(
            "handling" to handling.dbValue,
        ),
        EndringsloggTestMapper()
    )
}

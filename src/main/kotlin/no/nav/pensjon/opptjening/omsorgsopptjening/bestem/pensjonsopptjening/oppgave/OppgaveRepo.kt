package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.serialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Component
class OppgaveRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {
    fun persist(oppgave: Oppgave): Oppgave {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into oppgave (behandlingId, meldingId, detaljer) values (:behandlingId, :meldingId, to_json(:detaljer::json))""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "behandlingId" to oppgave.behandlingId,
                    "meldingId" to oppgave.meldingId,
                    "detaljer" to serialize(oppgave.detaljer)
                ),
            ),
            keyHolder
        )
        jdbcTemplate.update(
            """insert into oppgave_status (id, status, statushistorikk) values (:id, to_json(:status::json), to_json(:statushistorikk::json))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(oppgave.status),
                    "statushistorikk" to oppgave.statushistorikk.serialize()
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(oppgave: Oppgave) {
        jdbcTemplate.update(
            """update oppgave_status set status = to_json(:status::json), statushistorikk = to_json(:statushistorikk::json) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to oppgave.id!!,
                    "status" to serialize(oppgave.status),
                    "statushistorikk" to oppgave.statushistorikk.serialize()
                ),
            ),
        )
    }

    fun find(id: UUID): Oppgave {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId where o.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OmsorgsarbeidMessageRowMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<Oppgave> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId where o.meldingId = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OmsorgsarbeidMessageRowMapper()
        )
    }

    fun findForBehandling(id: UUID): List<Oppgave> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId join behandling b on b.id = o.behandlingId where b.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OmsorgsarbeidMessageRowMapper()
        )
    }

    fun existsForOmsorgsyterOgÅr(omsorgsyter: String, år: Int): Boolean {
        return jdbcTemplate.query(
            """select count(1) as antall from oppgave o join behandling b on b.id = o.behandlingId where b.omsorgsyter = :omsorgsyter and b.omsorgs_ar = :omsorgsar""",
            mapOf<String, Any>(
                "omsorgsyter" to omsorgsyter,
                "omsorgsar" to år
            ),
            ResultSetExtractor { rs -> if (rs.next()) rs.getInt("antall") > 0 else throw RuntimeException("Could not extract resultset") }
        )!!
    }

    fun existsForOmsorgsmottakerOgÅr(omsorgsmottaker: String, år: Int): Boolean {
        return jdbcTemplate.query(
            """select count(1) as antall from oppgave o join behandling b on b.id = o.behandlingId where b.omsorgsmottaker = :omsorgsmottaker and b.omsorgs_ar = :omsorgsar""",
            mapOf<String, Any>(
                "omsorgsmottaker" to omsorgsmottaker,
                "omsorgsar" to år
            ),
            ResultSetExtractor { rs -> if (rs.next()) rs.getInt("antall") > 0 else throw RuntimeException("Could not extract resultset") }
        )!!
    }

    fun finnNesteUprosesserte(): Oppgave? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId  where (os.status->>'type' = 'Klar') or (os.status->>'type' = 'Retry' and (os.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for update of o skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            OmsorgsarbeidMessageRowMapper()
        ).singleOrNull()
    }

    internal class OmsorgsarbeidMessageRowMapper : RowMapper<Oppgave> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Oppgave {
            return Oppgave(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                detaljer = deserialize(rs.getString("detaljer")),
                behandlingId = rs.getString("behandlingId")?.let { UUID.fromString(it) },
                meldingId = UUID.fromString(rs.getString("meldingId")),
                correlationId = UUID.fromString(rs.getString("correlation_id")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
            )
        }
    }
}
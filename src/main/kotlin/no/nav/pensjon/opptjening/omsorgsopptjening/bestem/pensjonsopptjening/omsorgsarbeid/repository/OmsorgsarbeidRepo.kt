package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.serialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
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
class OmsorgsarbeidRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {

    fun persist(melding: OmsorgsarbeidMelding): OmsorgsarbeidMelding {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into melding (melding, correlation_id) values (to_json(:melding::json), :correlation_id)""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "melding" to melding.melding,
                    "correlation_id" to melding.correlationId,
                ),
            ),
            keyHolder
        )
        jdbcTemplate.update(
            """insert into melding_status (id, status, statushistorikk) values (:id, to_json(:status::json), to_json(:statushistorikk::json))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(melding.status),
                    "statushistorikk" to melding.statushistorikk.serialize()
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(melding: OmsorgsarbeidMelding) {
        jdbcTemplate.update(
            """update melding_status set status = to_json(:status::json), statushistorikk = to_json(:statushistorikk::json) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to melding.id!!,
                    "status" to serialize(melding.status),
                    "statushistorikk" to melding.statushistorikk.serialize()
                ),
            ),
        )
    }

    fun find(id: UUID): OmsorgsarbeidMelding {
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where m.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OmsorgsarbeidMessageRowMapper()
        ).single()
    }

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */
    fun finnNesteUprosesserte(): OmsorgsarbeidMelding? {
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where (ms.status->>'type' = 'Klar') or (ms.status->>'type' = 'Retry' and (ms.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of m skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            OmsorgsarbeidMessageRowMapper()
        ).singleOrNull()
    }

    internal class OmsorgsarbeidMessageRowMapper : RowMapper<OmsorgsarbeidMelding> {
        override fun mapRow(rs: ResultSet, rowNum: Int): OmsorgsarbeidMelding {
            return OmsorgsarbeidMelding(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                melding = rs.getString("melding"),
                correlationId = rs.getString("correlation_id"),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
            )
        }
    }
}
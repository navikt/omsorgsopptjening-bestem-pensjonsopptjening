package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
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

    fun persist(melding: OmsorgsarbeidMelding.Lest): OmsorgsarbeidMelding.Mottatt {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into melding (melding, correlation_id, innlesing_id) values (to_jsonb(:melding::jsonb), :correlation_id, :innlesing_id)""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "melding" to serialize(melding.innhold),
                    "correlation_id" to melding.correlationId.toString(),
                    "innlesing_id" to melding.innlesingId.toString(),
                ),
            ),
            keyHolder
        )
        jdbcTemplate.update(
            """insert into melding_status (id, status, statushistorikk) values (:id, to_jsonb(:status::jsonb), to_jsonb(:statushistorikk::jsonb))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(melding.status),
                    "statushistorikk" to melding.statushistorikk.serializeList()
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(melding: OmsorgsarbeidMelding.Mottatt) {
        jdbcTemplate.update(
            """update melding_status set status = to_jsonb(:status::jsonb), statushistorikk = to_jsonb(:statushistorikk::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to melding.id,
                    "status" to serialize(melding.status),
                    "statushistorikk" to melding.statushistorikk.serializeList()
                ),
            ),
        )
    }

    fun find(id: UUID): OmsorgsarbeidMelding.Mottatt {
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
    fun finnNesteUprosesserte(): OmsorgsarbeidMelding.Mottatt? {
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where (ms.status->>'type' = 'Klar') or (ms.status->>'type' = 'Retry' and (ms.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of m skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            OmsorgsarbeidMessageRowMapper()
        ).singleOrNull()
    }

    internal class OmsorgsarbeidMessageRowMapper : RowMapper<OmsorgsarbeidMelding.Mottatt> {
        override fun mapRow(rs: ResultSet, rowNum: Int): OmsorgsarbeidMelding.Mottatt {
            return OmsorgsarbeidMelding.Mottatt(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                innhold = deserialize(rs.getString("melding")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
            )
        }
    }
}
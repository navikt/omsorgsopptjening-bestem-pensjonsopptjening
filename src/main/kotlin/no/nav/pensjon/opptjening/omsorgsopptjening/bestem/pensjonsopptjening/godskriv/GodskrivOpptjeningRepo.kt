package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv

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
class GodskrivOpptjeningRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {
    fun persist(godskrivOpptjening: GodskrivOpptjening): GodskrivOpptjening {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into godskriv_opptjening (behandlingId) values (:behandlingId)""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "behandlingId" to godskrivOpptjening.behandlingId,
                ),
            ),
            keyHolder
        )
        jdbcTemplate.update(
            """insert into godskriv_opptjening_status (id, status, statushistorikk) values (:id, to_json(:status::json), to_json(:statushistorikk::json))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serialize()
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(godskrivOpptjening: GodskrivOpptjening) {
        jdbcTemplate.update(
            """update godskriv_opptjening_status set status = to_json(:status::json), statushistorikk = to_json(:statushistorikk::json) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to godskrivOpptjening.id!!,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serialize()
                ),
            ),
        )
    }

    fun find(id: UUID): GodskrivOpptjening {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where o.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<GodskrivOpptjening> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where m.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        )
    }

    fun findForBehandling(id: UUID): List<GodskrivOpptjening> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk,  m.id as meldingid, m.correlation_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where b.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        )
    }

    fun finnNesteUprosesserte(): GodskrivOpptjening? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId  where (os.status->>'type' = 'Klar') or (os.status->>'type' = 'Retry' and (os.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for update of o skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            GodskrivOpptjeningMapper()
        ).singleOrNull()
    }


    internal class GodskrivOpptjeningMapper : RowMapper<GodskrivOpptjening> {
        override fun mapRow(rs: ResultSet, rowNum: Int): GodskrivOpptjening {
            return GodskrivOpptjening(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                behandlingId = rs.getString("behandlingId").let { UUID.fromString(it) },
                meldingId = UUID.fromString(rs.getString("meldingid")),
                correlationId = UUID.fromString(rs.getString("correlation_id")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
                omsorgsyter = rs.getString("omsorgsyter")
            )
        }
    }
}
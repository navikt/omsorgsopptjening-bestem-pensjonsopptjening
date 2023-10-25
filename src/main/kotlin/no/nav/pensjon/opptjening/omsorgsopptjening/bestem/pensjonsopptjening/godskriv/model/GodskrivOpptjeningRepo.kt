package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
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
class GodskrivOpptjeningRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {
    fun persist(godskrivOpptjening: GodskrivOpptjening.Transient): GodskrivOpptjening.Persistent {
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
            """insert into godskriv_opptjening_status (id, status, statushistorikk,kort_status) values (:id, to_jsonb(:status::jsonb), to_jsonb(:statushistorikk::jsonb),:kort_status)""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serializeList(),
                    "kort_status" to godskrivOpptjening.kortStatus.toString(),
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(godskrivOpptjening: GodskrivOpptjening.Persistent) {
        jdbcTemplate.update(
            """update godskriv_opptjening_status set status = to_jsonb(:status::jsonb), statushistorikk = to_jsonb(:statushistorikk::jsonb),kort_status = :kort_status where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to godskrivOpptjening.id,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serializeList(),
                    "kort_status" to godskrivOpptjening.kortStatus.toString(),
                ),
            ),
        )
    }

    fun find(id: UUID): GodskrivOpptjening.Persistent {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where o.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<GodskrivOpptjening.Persistent> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where m.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        )
    }

    fun findForBehandling(id: UUID): List<GodskrivOpptjening.Persistent> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk,  m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where b.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        )
    }

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */
    fun finnNesteUprosesserte(): GodskrivOpptjening.Persistent? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter from godskriv_opptjening o join godskriv_opptjening_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId  where (os.status->>'type' = 'Klar') or (os.status->>'type' = 'Retry' and (os.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of o skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            GodskrivOpptjeningMapper()
        ).singleOrNull()
    }


    internal class GodskrivOpptjeningMapper : RowMapper<GodskrivOpptjening.Persistent> {
        override fun mapRow(rs: ResultSet, rowNum: Int): GodskrivOpptjening.Persistent {
            return GodskrivOpptjening.Persistent(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                behandlingId = rs.getString("behandlingId").let { UUID.fromString(it) },
                meldingId = UUID.fromString(rs.getString("meldingid")),
                correlationId = CorrelationId.fromString(rs.getString("correlation_id")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
                omsorgsyter = rs.getString("omsorgsyter"),
                innlesingId = InnlesingId.fromString(rs.getString("innlesing_id"))
            )
        }
    }
}
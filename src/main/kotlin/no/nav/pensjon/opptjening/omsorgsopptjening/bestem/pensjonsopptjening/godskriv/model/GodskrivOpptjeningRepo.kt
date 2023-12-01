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
            """insert into godskriv_opptjening_status (id, status, status_type, karantene_til, statushistorikk) 
                |values (:id, to_jsonb(:status::jsonb), :status_type, :karantene_til::timestamptz, to_jsonb(:statushistorikk::jsonb))""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serializeList(),
                    "status_type" to when(godskrivOpptjening.status) {
                        is GodskrivOpptjening.Status.Feilet -> "Feilet"
                        is GodskrivOpptjening.Status.Ferdig -> "Ferdig"
                        is GodskrivOpptjening.Status.Klar -> "Klar"
                        is GodskrivOpptjening.Status.Retry -> "Retry"
                    },
                    "karantene_til" to when(val s = godskrivOpptjening.status) {
                        is GodskrivOpptjening.Status.Retry -> s.karanteneTil
                        else -> null
                    }
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(godskrivOpptjening: GodskrivOpptjening.Persistent) {
        jdbcTemplate.update(
            """update godskriv_opptjening_status 
                | set status = to_jsonb(:status::jsonb),
                | status_type = :status_type,
                | karantene_til =:karantene_til::timestamptz,
                |statushistorikk = to_jsonb(:statushistorikk::jsonb) 
                |where id = :id""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to godskrivOpptjening.id,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serializeList(),
                    "status_type" to when(godskrivOpptjening.status) {
                        is GodskrivOpptjening.Status.Feilet -> "Feilet"
                        is GodskrivOpptjening.Status.Ferdig -> "Ferdig"
                        is GodskrivOpptjening.Status.Klar -> "Klar"
                        is GodskrivOpptjening.Status.Retry -> "Retry"
                    },
                    "karantene_til" to when(val s = godskrivOpptjening.status) {
                        is GodskrivOpptjening.Status.Retry -> s.karanteneTil.toString()
                        else -> null
                    }
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
    fun finnNesteUprosesserte(antall: Int): List<GodskrivOpptjening.Persistent> {
        val now = Instant.now(clock)
        val neste = finnNesteUprosesserteKlar(now, antall).ifEmpty { finnNesteUprosesserteRetry(now, antall) }
        return neste.map { find(it) }
    }

    fun finnNesteUprosesserteKlar(now: Instant, antall: Int): List<UUID> {
        return jdbcTemplate.queryForList(
            """select id
                | from godskriv_opptjening_status
                | where status_type = 'Klar' 
                | fetch first :antall rows only for no key update skip locked""".trimMargin(),
            mapOf(
                "now" to now.toString(),
                "antall" to antall,
            ),
            UUID::class.java
        )
    }

    fun finnNesteUprosesserteRetry(now: Instant, antall: Int): List<UUID> {
        return jdbcTemplate.queryForList(
            """select id
                | from godskriv_opptjening_status
                | where status_type = 'Retry'
                | and karantene_til::timestamptz < (:now)::timestamptz
                | fetch first :antall rows only for no key update skip locked""".trimMargin(),
            mapOf(
                "now" to now.toString(),
                "antall" to antall,
            ),
            UUID::class.java
        )
    }


    fun finnEldsteIkkeFerdig(): GodskrivOpptjening.Persistent? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter 
                |from godskriv_opptjening o,godskriv_opptjening_status os, behandling b,melding m
                |where o.id = os.id and b.id = o.behandlingId and m.id = b.kafkaMeldingId  
                |and (os.status->>'type' <> 'Ferdig') order by o.opprettet asc limit 1""".trimMargin(),
            GodskrivOpptjeningMapper()
        ).singleOrNull()
    }

    fun finnEttEllerAnnetKarantene(): GodskrivOpptjening.Persistent? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter 
                |from godskriv_opptjening o,godskriv_opptjening_status os, behandling b,melding m
                |where o.id = os.id and b.id = o.behandlingId and m.id = b.kafkaMeldingId  
                |and (os.status->>'type' <> 'Ferdig') and (os.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of o skip locked""".trimMargin(),
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
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
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@Component
class GodskrivOpptjeningRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {
    private fun GodskrivOpptjening.Status.databaseName(): String {
        return when (this) {
            is GodskrivOpptjening.Status.Feilet -> "Feilet"
            is GodskrivOpptjening.Status.Ferdig -> "Ferdig"
            is GodskrivOpptjening.Status.Klar -> "Klar"
            is GodskrivOpptjening.Status.Retry -> "Retry"
        }
    }

    private fun GodskrivOpptjening.Status.karanteneTilString(): String? {
        return when (val s = this) {
            is GodskrivOpptjening.Status.Retry -> s.karanteneTil.toString()
            else -> null
        }
    }

    fun persist(godskrivOpptjening: GodskrivOpptjening.Transient): GodskrivOpptjening.Persistent {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into godskriv_opptjening (behandlingId,status, karantene_til, statushistorikk)
                |values (:behandlingId,:status, :karantene_til::timestamptz, to_jsonb(:statushistorikk::jsonb))""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "behandlingId" to godskrivOpptjening.behandlingId,
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serializeList(),
                    "status" to godskrivOpptjening.status.databaseName(),
                    "karantene_til" to godskrivOpptjening.status.karanteneTilString(),
                ),
            ),
            keyHolder
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(godskrivOpptjening: GodskrivOpptjening.Persistent) {
        jdbcTemplate.update(
            """update godskriv_opptjening
                | set status = :status,
                | karantene_til =:karantene_til::timestamptz,
                | statushistorikk = to_jsonb(:statushistorikk::jsonb)
                | where id = :id""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to godskrivOpptjening.id,
                    "status" to serialize(godskrivOpptjening.status),
                    "statushistorikk" to godskrivOpptjening.statushistorikk.serializeList(),
                    "status" to godskrivOpptjening.status.databaseName(),
                    "karantene_til" to godskrivOpptjening.status.karanteneTilString(),
                ),
            ),
        )
    }

    fun find(id: UUID): GodskrivOpptjening.Persistent {
        return jdbcTemplate.query(
            """select o.*, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter 
                |from godskriv_opptjening o 
                |join behandling b on b.id = o.behandlingId 
                |join melding m on m.id = b.kafkaMeldingId 
                |where o.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<GodskrivOpptjening.Persistent> {
        return jdbcTemplate.query(
            """select o.*, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter 
                |from godskriv_opptjening o 
                |join behandling b on b.id = o.behandlingId 
                |join melding m on m.id = b.kafkaMeldingId 
                |where m.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            GodskrivOpptjeningMapper()
        )
    }

    fun findForBehandling(id: UUID): List<GodskrivOpptjening.Persistent> {
        return jdbcTemplate.query(
            """select o.*,  m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter 
                |from godskriv_opptjening o 
                |join behandling b on b.id = o.behandlingId 
                |join melding m on m.id = b.kafkaMeldingId 
                |where b.id = :id""".trimMargin(),
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
    fun finnNesteUprosesserte(antall: Int): Locked {
        val lockId = UUID.randomUUID()
        val now = Instant.now(clock)
        val neste =
            finnNesteUprosesserteKlar(lockId, now, antall).ifEmpty { finnNesteUprosesserteRetry(lockId, now, antall) }
        return Locked(lockId, neste.map { find(it) })
    }

    fun frigi(locked: Locked) {
        jdbcTemplate.update(
            """update godskriv_opptjening set lockId = null, lockTime = null where lockId = :lockId""",
            mapOf<String, Any>(
                "lockId" to locked.lockId
            )
        )
    }

    fun frigiGamleLåser() {
        val oneHourAgo = Instant.now(clock).minus(1.hours.toJavaDuration()).toString()
        jdbcTemplate.update(
            """update godskriv_opptjening set lockId = null, lockTime = null 
            |where lockId is not null and lockTime < :oneHourAgo::timestamptz""".trimMargin(),
            mapOf<String, Any>(
                "oneHourAgo" to oneHourAgo
            )
        )
    }

    fun finnNesteUprosesserteKlar(lockId: UUID, now: Instant, antall: Int): List<UUID> {
        jdbcTemplate.update(
            """update godskriv_opptjening set lockId = :lockId, lockTime = :now::timestamptz
                |where id in (
                |select id
                | from godskriv_opptjening
                | where status = 'Klar'
                |   and lockId is null
                | order by id
                | fetch first :antall rows only for no key update skip locked)""".trimMargin(),
            mapOf(
                "now" to now.toString(),
                "antall" to antall,
                "lockId" to lockId,
            )
        )

        return jdbcTemplate.queryForList(
            """select id from godskriv_opptjening where lockId = :lockId""",
            mapOf(
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }

    fun finnNesteUprosesserteRetry(lockId: UUID, now: Instant, antall: Int): List<UUID> {
        jdbcTemplate.update(
            """update godskriv_opptjening set lockId = :lockId, lockTime = :now::timestamptz
                |where id in (
                |select id
                | from godskriv_opptjening
                | where status = 'Retry'
                | and karantene_til::timestamptz < (:now)::timestamptz
                | and karantene_til is not null
                | and lockId is null
                | order by karantene_til
                | fetch first :antall rows only for no key update skip locked)""".trimMargin(),
            mapOf(
                "now" to now.toString(),
                "antall" to antall,
                "lockId" to lockId,
            )
        )

        return jdbcTemplate.queryForList(
            """select id from godskriv_opptjening where lockId = :lockId""",
            mapOf(
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }


    fun finnEldsteIkkeFerdig(): GodskrivOpptjening.Persistent? {
        return jdbcTemplate.query(
            """select o.*, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter 
                |from godskriv_opptjening o,behandling b,melding m
                |where b.id = o.behandlingId and m.id = b.kafkaMeldingId  
                |and (o.status <> 'Ferdig') order by o.opprettet asc limit 1""".trimMargin(),
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

    data class Locked(var lockId: UUID, val data: List<GodskrivOpptjening.Persistent>)
}
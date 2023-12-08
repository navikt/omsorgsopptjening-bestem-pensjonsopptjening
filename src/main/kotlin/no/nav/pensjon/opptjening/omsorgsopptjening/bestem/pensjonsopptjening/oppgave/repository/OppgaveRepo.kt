package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.*
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.Clock
import java.time.Instant
import java.util.*

@Component
class OppgaveRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {
    private fun Oppgave.Status.databaseName(): String {
        return when (this) {
            is Oppgave.Status.Feilet -> "Feilet"
            is Oppgave.Status.Ferdig -> "Ferdig"
            is Oppgave.Status.Klar -> "Klar"
            is Oppgave.Status.Retry -> "Retry"
        }
    }

    private fun Oppgave.Status.karanteneTilString(): String? {
        return when (val s = this) {
            is Oppgave.Status.Retry -> s.karanteneTil.toString()
            else -> null
        }
    }

    fun persist(oppgave: Oppgave.Transient): Oppgave.Persistent {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into oppgave (behandlingId, meldingId, detaljer, status, karantene_til, statushistorikk) 
                |values (:behandlingId, :meldingId, to_jsonb(:detaljer::jsonb), :status, :karantene_til::timestamptz, to_jsonb(:statushistorikk::jsonb))""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "behandlingId" to oppgave.behandlingId,
                    "meldingId" to oppgave.meldingId,
                    "detaljer" to serialize(oppgave.detaljer),
                    "statushistorikk" to oppgave.statushistorikk.serializeList(),
                    "status" to oppgave.status.databaseName(),
                    "karantene_til" to oppgave.status.karanteneTilString(),
                ),
            ),
            keyHolder
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(oppgave: Oppgave.Persistent) {
        jdbcTemplate.update(
            """update oppgave
                | set status = :status,
                | karantene_til = :karantene_til::timestamptz,
                | statushistorikk = to_jsonb(:statushistorikk::jsonb) 
                | where id = :id""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to oppgave.id,
                    "status" to serialize(oppgave.status),
                    "statushistorikk" to oppgave.statushistorikk.serializeList(),
                    "status" to oppgave.status.databaseName(),
                    "karantene_til" to oppgave.status.karanteneTilString(),
                ),
            ),
        )
    }

    fun find(id: UUID): Oppgave.Persistent {
        return jdbcTemplate.query(
            """select o.*, m.correlation_id, m.innlesing_id
                | from oppgave o
                | join melding m on m.id = o.meldingId
                | where o.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            OppgaveMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<Oppgave.Persistent> {
        return jdbcTemplate.query(
            """select o.*, m.correlation_id, m.innlesing_id 
                |from oppgave o 
                |join melding m on m.id = o.meldingId 
                |where o.meldingId = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            OppgaveMapper()
        )
    }

    fun findForBehandling(id: UUID): List<Oppgave.Persistent> {
        return jdbcTemplate.query(
            """select o.*, m.correlation_id, m.innlesing_id 
                |from oppgave o 
                |join melding m on m.id = o.meldingId 
                |join behandling b on b.id = o.behandlingId 
                |where b.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            OppgaveMapper()
        )
    }

    fun existsForOmsorgsyterOgÅr(omsorgsyter: String, år: Int): Boolean {
        return jdbcTemplate.query(
            """select count(1) as antall from oppgave o 
                |join behandling b on b.id = o.behandlingId
                | where b.omsorgsyter = :omsorgsyter and b.omsorgs_ar = :omsorgsar""".trimMargin(),
            mapOf<String, Any>(
                "omsorgsyter" to omsorgsyter,
                "omsorgsar" to år
            ),
            ResultSetExtractor { rs -> if (rs.next()) rs.getInt("antall") > 0 else throw RuntimeException("Could not extract resultset") }
        )!!
    }

    fun existsForOmsorgsmottakerOgÅr(omsorgsmottaker: String, år: Int): Boolean {
        return jdbcTemplate.query(
            """select count(1) as antall from oppgave o 
                |join behandling b on b.id = o.behandlingId 
                |where b.omsorgsmottaker = :omsorgsmottaker 
                |and b.omsorgs_ar = :omsorgsar""".trimMargin(),
            mapOf<String, Any>(
                "omsorgsmottaker" to omsorgsmottaker,
                "omsorgsar" to år
            ),
            ResultSetExtractor { rs -> if (rs.next()) rs.getInt("antall") > 0 else throw RuntimeException("Could not extract resultset") }
        )!!
    }

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */

    fun finnNesteUprosesserte(antall: Int): Locked {
        val lockId = UUID.randomUUID()
        val now = Instant.now(clock)
        val oppgave: List<UUID> =
            finnNesteUprosesserteKlar(lockId, now, antall).ifEmpty { finnNesteUprosesserteRetry(lockId, now, antall) }
        return Locked(lockId, oppgave.map { find(it) })
    }

    fun frigi(locked: Locked) {
        jdbcTemplate.update(
            """update oppgave set lockId = null, lockTime = null where lockId = :lockId""",
            mapOf<String, Any>(
                "lockId" to locked.lockId
            )
        )
    }

    fun finnNesteUprosesserteKlar(lockId: UUID, now: Instant, antall: Int): List<UUID> {
        jdbcTemplate.update(
            """update oppgave
                |set lockId = :lockId, lockTime = :now::timestamptz
                |where id in (
                |select id
                | from oppgave
                | where status = 'Klar'
                |   and lockId is null
                | order by id
                | fetch first :antall rows only for no key update skip locked)""".trimMargin(),
            mapOf(
                "antall" to antall,
                "now" to now.toString(),
                "lockId" to lockId,
            ),
        )

        return jdbcTemplate.queryForList(
            """select id from oppgave where lockId = :lockId""",
            mapOf(
                "now" to now.toString(),
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }

    fun finnNesteUprosesserteRetry(lockId: UUID, now: Instant, antall: Int): List<UUID> {
        jdbcTemplate.update(
            """update oppgave
                |set lockId = :lockId, lockTime = :now::timestamptz
                |where id in (
                |select id
                | from oppgave
                | where status = 'Retry'
                |   and karantene_til < (:now)::timestamptz
                |   and karantene_til is not null
                |   and lockId is null
                |   order by karantene_til
                | fetch first :antall rows only for no key update skip locked)""".trimMargin(),
            mapOf(
                "now" to now.toString(),
                "lockId" to lockId,
                "antall" to antall,
            )
        )

        return jdbcTemplate.queryForList(
            """select id from oppgave where lockId = :lockId""",
            mapOf(
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }

    fun finnEldsteUbehandledeOppgave(): Oppgave.Persistent? {
        return jdbcTemplate.query(
            """select o.*, m.correlation_id, m.innlesing_id 
            |from oppgave o, melding m
            |where m.id = o.meldingId
            |order by o.opprettet asc limit 1""".trimMargin(),
            mapOf(
                "ferdig" to Oppgave.Status.Ferdig::class.simpleName
            ), OppgaveMapper()
        ).singleOrNull()
    }

    internal class OppgaveMapper : RowMapper<Oppgave.Persistent> {

        override fun mapRow(rs: ResultSet, rowNum: Int): Oppgave.Persistent {
            return Oppgave.Persistent(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                detaljer = deserialize(rs.getString("detaljer")),
                behandlingId = rs.getString("behandlingId")?.let { UUID.fromString(it) },
                meldingId = UUID.fromString(rs.getString("meldingId")),
                correlationId = CorrelationId.fromString(rs.getString("correlation_id")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
                innlesingId = InnlesingId.fromString(rs.getString("innlesing_id"))
            )
        }
    }

    data class Locked(val lockId: UUID, val data: List<Oppgave.Persistent>)
}
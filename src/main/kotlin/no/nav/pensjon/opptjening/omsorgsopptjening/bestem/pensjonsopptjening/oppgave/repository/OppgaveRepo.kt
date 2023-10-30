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
    fun persist(oppgave: Oppgave.Transient): Oppgave.Persistent {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into oppgave (behandlingId, meldingId, detaljer) values (:behandlingId, :meldingId, to_jsonb(:detaljer::jsonb))""",
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
            """insert into oppgave_status (id, status, statushistorikk) values (:id, to_jsonb(:status::jsonb), to_jsonb(:statushistorikk::jsonb))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(oppgave.status),
                    "statushistorikk" to oppgave.statushistorikk.serializeList(),
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(oppgave: Oppgave.Persistent) {
        jdbcTemplate.update(
            """update oppgave_status set status = to_jsonb(:status::jsonb), statushistorikk = to_jsonb(:statushistorikk::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to oppgave.id,
                    "status" to serialize(oppgave.status),
                    "statushistorikk" to oppgave.statushistorikk.serializeList()
                ),
            ),
        )
    }

    fun find(id: UUID): Oppgave.Persistent {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id, m.innlesing_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId where o.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OppgaveMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<Oppgave.Persistent> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id, m.innlesing_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId where o.meldingId = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OppgaveMapper()
        )
    }

    fun findForBehandling(id: UUID): List<Oppgave.Persistent> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id, m.innlesing_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId join behandling b on b.id = o.behandlingId where b.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OppgaveMapper()
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

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */
    fun finnNesteUprosesserte(): Oppgave.Persistent? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id, m.innlesing_id from oppgave o join oppgave_status os on o.id = os.id join melding m on m.id = o.meldingId  where (os.status->>'type' = 'Klar') or (os.status->>'type' = 'Retry' and (os.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of o skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            OppgaveMapper()
        ).singleOrNull()
    }

    fun finnEldsteUbehandledeOppgave(): Oppgave.Persistent? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id, m.innlesing_id 
            |from oppgave o, oppgave_status os, melding m
            |where o.id = os.id and m.id = o.meldingId
            |order by o.opprettet asc limit 1""".trimMargin(),
//            |and (os.status->>'type' <> :ferdig) order by o.opprettet asc limit 1""".trimMargin(),
            mapOf(
                "ferdig" to Oppgave.Status.Ferdig::class.simpleName
            ), OppgaveMapper()
        ).singleOrNull()
    }

    fun finnOppgaverIForLangKarantene(): Oppgave? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.correlation_id, m.innlesing_id 
            |from oppgave o, oppgave_status os, melding m
            |where o.id = os.id and m.id = o.meldingId
            |and (os.status->>'type' <> :karanteneTil) order by o.opprettet asc limit 1""".trimMargin(),
            mapOf(
                "ferdig" to Instant.now(clock).toString()
            ), OppgaveMapper()
        ).singleOrNull()
    }


    internal class OppgaveMapper : RowMapper<Oppgave.Persistent> {

        override fun mapRow(rs: ResultSet, rowNum: Int): Oppgave.Persistent {

            println("*** Deserialize oppgave: ${rs.getString("statushistorikk")}")

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
}
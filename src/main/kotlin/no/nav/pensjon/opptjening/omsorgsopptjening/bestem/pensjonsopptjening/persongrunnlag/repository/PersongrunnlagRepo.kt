package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
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
import kotlin.reflect.KClass

@Component
class PersongrunnlagRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {

    fun persist(melding: PersongrunnlagMelding.Lest): PersongrunnlagMelding.Mottatt {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into melding (melding, correlation_id, innlesing_id, opprettet) values (to_jsonb(:melding::jsonb), :correlation_id, :innlesing_id, :opprettet::timestamptz)""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "melding" to serialize(melding.innhold),
                    "correlation_id" to melding.correlationId.toString(),
                    "innlesing_id" to melding.innlesingId.toString(),
                    "opprettet" to melding.opprettet.toString(),
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
                    "statushistorikk" to melding.statushistorikk.serializeList(),
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(melding: PersongrunnlagMelding.Mottatt) {
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

    fun find(id: UUID): PersongrunnlagMelding.Mottatt {
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where m.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            PersongrunnlagMeldingMapper()
        ).single()
    }

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */
    fun finnNesteUprosesserte(): PersongrunnlagMelding.Mottatt? {
        return jdbcTemplate.query(
//            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where (ms.status->>'type' = 'Klar') or (ms.status->>'type' = 'Retry' and (ms.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of m skip locked""",
            /*
            """select m.*, ms.statushistorikk 
               | from melding m 
               | join (select * from melding_status
               |       where (status->>'type' = 'Klar') or (status->>'type' = 'Retry')) ms on m.id = ms.id 
               | where (ms.status->>'type' = 'Klar') 
               | or (ms.status->>'type' = 'Retry' and (ms.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) 
               | fetch first row only for no key update of m skip locked""".trimMargin(),
             */
            """ SELECT mms.*, m.id as m_id2
                  | FROM (SELECT *, ms.statushistorikk, ms.status as ms_status, (ms.status->>'karanteneTil') as karanteneTil, m.id as m_id1
                  |        FROM melding m,
                  |             (SELECT * FROM melding_status WHERE (status->>'type' = 'Klar') OR (status->>'type' = 'Retry')) ms
                  |        WHERE ms.id = m.id) mms,
                  |        melding m
                  |  WHERE karanteneTil is null or (karanteneTil)::timestamptz < (:now)::timestamptz
                  |    AND m.id = mms.m_id1
                  | FETCH FIRST ROW ONLY FOR NO KEY UPDATE OF m SKIP LOCKED""".trimMargin(),
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnSiste() : PersongrunnlagMelding? {
        return jdbcTemplate.query(
//            """select m.*, ms.statushistorikk from melding m, melding_status ms
//                |where m.id = ms.id and order by m.opprettet desc limit 1""".trimMargin(),
              """select m.*, ms.statushistorikk from
                  | (select * from melding m order by m.opprettet desc limit 1) m,
                  | melding_status ms
                  | where m.id = ms.id""".trimMargin(),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnEldsteMedStatus(kclass: KClass<*>) : PersongrunnlagMelding? {
        val name = kclass.simpleName!!
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m, melding_status ms
                |where m.id = ms.id and (status->>'type' = :type) 
                |order by m.opprettet asc limit 1""".trimMargin(),
            mapOf(
                "type" to name
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnEldsteSomIkkeErFerdig() : PersongrunnlagMelding? {
        val name = PersongrunnlagMelding.Status.Ferdig::class.simpleName!!
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m, melding_status ms
                |where m.id = ms.id and (status->>'type' <> :type) 
                |order by m.opprettet asc limit 1""".trimMargin(),
            mapOf(
                "type" to name
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun antallMedStatus(kclass: KClass<*>) : Long {
        val name = kclass.simpleName!!
        return jdbcTemplate.queryForObject(
            """select count(*) from melding_status where (status->>'type' = :type)""",
            mapOf(
                "type" to name
            ),
            Long::class.java
        )!!
    }

    internal class PersongrunnlagMeldingMapper : RowMapper<PersongrunnlagMelding.Mottatt> {
        override fun mapRow(rs: ResultSet, rowNum: Int): PersongrunnlagMelding.Mottatt {
            return PersongrunnlagMelding.Mottatt(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                innhold = deserialize(rs.getString("melding")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
            )
        }
    }
}
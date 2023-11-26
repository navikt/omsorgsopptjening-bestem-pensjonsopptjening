package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * @return Id til rad som ble lagret i databasen - null dersom raden eksisterte fra før (duplikat)
     */
    fun lagre(melding: PersongrunnlagMelding.Lest): UUID? {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            //language=postgres-psql
            """
                |with pg as (insert into melding (melding, correlation_id, innlesing_id, opprettet) 
                |values (to_jsonb(:melding::jsonb), :correlation_id, :innlesing_id, :opprettet::timestamptz) 
                |on conflict on constraint unique_correlation_innlesing do nothing 
                |returning id, to_jsonb(:status::jsonb) as status, to_jsonb(:statushistorikk::jsonb) as statushistorikk, :statusType as statusType, :karanteneTil::timestamptz as karanteneTil)
                |insert into melding_status (id, status, statushistorikk, status_type, karantene_til)
                |select id, status, statushistorikk, statusType, karanteneTil from pg where id is not null
                |returning (select id from pg)
            """
                .trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "melding" to serialize(melding.innhold),
                    "correlation_id" to melding.correlationId.toString(),
                    "innlesing_id" to melding.innlesingId.toString(),
                    "opprettet" to melding.opprettet.toString(),
                    "status" to serialize(melding.status),
                    "statusType" to "Klar",
                    "karanteneTil" to null,
                    "statushistorikk" to melding.statushistorikk.serializeList(),
                ),
            ),
            keyHolder
        )
        return keyHolder.getKeyAs(UUID::class.java)
            .also { if (it == null) log.info("Ingen primærnøkkel returnert fra insert, meldingen med correlationId:${melding.correlationId}, innlesingId:${melding.innlesingId} er et duplikat") }
    }

    fun updateStatus(melding: PersongrunnlagMelding.Mottatt) {
        jdbcTemplate.update(
            """update melding_status set status = to_jsonb(:status::jsonb), status_type = :statusType, karantene_til = :karanteneTil::timestamptz, statushistorikk = to_jsonb(:statushistorikk::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to melding.id,
                    "status" to serialize(melding.status),
                    "statusType" to when (melding.status) {
                        is PersongrunnlagMelding.Status.Feilet -> "Feilet"
                        is PersongrunnlagMelding.Status.Ferdig -> "Ferdig"
                        is PersongrunnlagMelding.Status.Klar -> "Klar"
                        is PersongrunnlagMelding.Status.Retry -> "Retry"
                    },
                    "karanteneTil" to when (val m = melding.status) {
                        is PersongrunnlagMelding.Status.Retry -> m.karanteneTil.toString()
                        else -> null
                    },
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
    fun finnNesteKlarTilProsessering(): PersongrunnlagMelding.Mottatt? {

        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk 
             |from melding m, melding_status ms 
             |where m.id = ms.id
             |and ms.status_type = 'Klar' 
             |order by m.id
             |fetch first row only for no key update of m skip locked""".trimMargin(),
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnNesteKlarForRetry(): PersongrunnlagMelding.Mottatt? {
        val now = Instant.now(clock).toString()
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk 
             |from melding m, melding_status ms 
             |where m.id = ms.id
             |and status_type = 'Retry'
             |and karantene_til is not null 
             |and karantene_til < (:now)::timestamptz
             |order by karantene_til
             |fetch first row only for no key update of m skip locked""".trimMargin(),
            mapOf(
                "now" to now
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnSiste(): PersongrunnlagMelding? {
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

    fun finnEldsteMedStatus(kclass: KClass<*>): PersongrunnlagMelding? {
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

    fun finnEldsteSomIkkeErFerdig(): PersongrunnlagMelding? {
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

    fun antallMedStatus(kclass: KClass<*>): Long {
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

    internal class SimpleStringMapper : RowMapper<String> {
        override fun mapRow(rs: ResultSet, rowNum: Int): String {
            return rs.toString()
        }
    }
}
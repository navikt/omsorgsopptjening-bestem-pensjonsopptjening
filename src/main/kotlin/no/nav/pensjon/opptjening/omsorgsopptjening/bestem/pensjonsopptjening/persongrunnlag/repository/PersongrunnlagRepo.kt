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
import java.util.*
import kotlin.reflect.KClass

@Component
class PersongrunnlagRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    private fun PersongrunnlagMelding.Status.databaseName(): String {
        return when (this) {
            is PersongrunnlagMelding.Status.Feilet -> "Feilet"
            is PersongrunnlagMelding.Status.Ferdig -> "Ferdig"
            is PersongrunnlagMelding.Status.Klar -> "Klar"
            is PersongrunnlagMelding.Status.Retry -> "Retry"
        }
    }

    private fun PersongrunnlagMelding.Status.karanteneTilString(): String? {
        return when (val s = this) {
            is PersongrunnlagMelding.Status.Retry -> s.karanteneTil.toString()
            else -> null
        }
    }


    /**
     * @return Id til rad som ble lagret i databasen - null dersom raden eksisterte fra før (duplikat)
     */
    fun lagre(melding: PersongrunnlagMelding.Lest): UUID? {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            //language=postgres-psql
            """insert into melding (melding, correlation_id, innlesing_id, opprettet, statushistorikk, status, karantene_til) 
              |values (to_jsonb(:melding::jsonb), :correlation_id, :innlesing_id, :opprettet::timestamptz, to_jsonb(:statushistorikk::jsonb), :status, :karanteneTil::timestamptz) 
              |on conflict on constraint unique_correlation_innlesing do nothing 
              |returning id
            """.trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "melding" to serialize(melding.innhold),
                    "correlation_id" to melding.correlationId.toString(),
                    "innlesing_id" to melding.innlesingId.toString(),
                    "opprettet" to melding.opprettet.toString(),
                    "status" to "Klar",
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
            """update melding set status = :status, karantene_til = :karanteneTil::timestamptz, statushistorikk = to_jsonb(:statushistorikk::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to melding.id,
                    "status" to melding.status.databaseName(),
                    "karanteneTil" to melding.status.karanteneTilString(),
                    "statushistorikk" to melding.statushistorikk.serializeList()
                ),
            ),
        )
    }

    fun find(id: UUID): PersongrunnlagMelding.Mottatt {
        return jdbcTemplate.query(
            """select * 
                |from melding 
                |where id = :id""".trimMargin(),
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
    fun finnNesteKlarTilProsessering(lockId: UUID, antall: Int): List<UUID> {

        // todo: legge på locktime
        jdbcTemplate.update(
            """update melding set lockId = :lockId
             | where id in (
             |   select id 
             |   from melding
             |   where status = 'Klar'
             |   and lockId is null
             |   order by id
             |   fetch first :antall rows only
             |   for update skip locked)""".trimMargin(),
            mapOf(
                "antall" to antall,
                "lockId" to lockId,
            )
        )

        return jdbcTemplate.queryForList(
            """select id 
             | from melding
             | where lockId = :lockId""".trimMargin(),
            mapOf(
                "lockId" to lockId
            ),
            UUID::class.java
        )
    }

    fun finnNesteKlarForRetry(lockId: UUID, antall: Int): List<UUID> {
        val now = Instant.now(clock).toString()

        jdbcTemplate.update(
            """update melding set lockId = :lockId
             | where id in (
             |   select id 
             |   from melding
             |   where status = 'Retry'
             |   and karantene_til is not null
             |   and karantene_til < (:now)::timestamptz
             |   and lockId is null
             |   order by karantene_til
             |   fetch first :antall rows only
             |   for update skip locked)""".trimMargin(),
            mapOf(
                "now" to now,
                "antall" to antall,
                "lockId" to lockId,
            )
        )

        return jdbcTemplate.queryForList(
            """select id 
             | from melding
             | where lockId = :lockId""".trimMargin(),
            mapOf(
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }

    fun frigi(lockId: UUID) {
        println("frigi $lockId")
        jdbcTemplate.update(
            """update melding set lockId = null where lockId = :lockId""",
            mapOf(
                "lockId" to lockId,
            ),
        )
    }

    fun finnSiste(): PersongrunnlagMelding? {
        return jdbcTemplate.query(
            """select * from melding order by opprettet desc limit 1""".trimMargin(),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnEldsteMedStatus(kclass: KClass<*>): PersongrunnlagMelding? {
        val name = kclass.simpleName!!
        return jdbcTemplate.query(
            """select * from melding
                |where status = :type 
                |order by opprettet asc limit 1""".trimMargin(),
            mapOf(
                "type" to name
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun finnEldsteSomIkkeErFerdig(): PersongrunnlagMelding? {
        val name = PersongrunnlagMelding.Status.Ferdig::class.simpleName!!
        return jdbcTemplate.query(
            """select * from melding
                | where status <> :type
                | order by opprettet asc limit 1""".trimMargin(),
            mapOf(
                "type" to name
            ),
            PersongrunnlagMeldingMapper()
        ).singleOrNull()
    }

    fun antallMedStatus(kclass: KClass<*>): Long {
        val name = kclass.simpleName!!
        return jdbcTemplate.queryForObject(
            """select count(*) from melding where status = :type""",
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
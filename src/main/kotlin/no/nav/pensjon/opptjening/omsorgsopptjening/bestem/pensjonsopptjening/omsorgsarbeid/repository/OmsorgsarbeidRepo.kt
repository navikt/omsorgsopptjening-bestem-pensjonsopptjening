package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.PersistertKafkaMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.Clock
import java.time.Instant
import java.util.UUID

inline fun <reified T> List<T>.serialize(): String {
    val listType = mapper.typeFactory.constructCollectionLikeType(List::class.java, T::class.java)
    return mapper.writerFor(listType).writeValueAsString(this)
}

inline fun <reified T> String.deserializeList(): List<T> {
    val listType = mapper.typeFactory.constructCollectionLikeType(List::class.java, T::class.java)
    return mapper.readerFor(listType).readValue(this)
}

@Component
class OmsorgsarbeidRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {

    fun persist(melding: PersistertKafkaMelding): PersistertKafkaMelding {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into melding (melding, correlation_id) values (:melding, :correlation_id)""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "melding" to melding.melding,
                    "correlation_id" to melding.correlationId,
                ),
            ),
            keyHolder
        )
        jdbcTemplate.update(
            """insert into melding_status (id, status, statushistorikk) values (:id, to_json(:status::json), to_json(:statushistorikk::json))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(melding.status),
                    "statushistorikk" to melding.statushistorikk.serialize()
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(melding: PersistertKafkaMelding) {
        jdbcTemplate.update(
            """update melding_status set status = to_json(:status::json), statushistorikk = to_json(:statushistorikk::json) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to melding.id!!,
                    "status" to serialize(melding.status),
                    "statushistorikk" to melding.statushistorikk.serialize()
                ),
            ),
        )
    }

    fun find(id: UUID): PersistertKafkaMelding {
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where m.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            OmsorgsarbeidMessageRowMapper()
        ).single()
    }

    fun finnNesteUprosesserte(): PersistertKafkaMelding? {
        return jdbcTemplate.query(
            """select m.*, ms.statushistorikk from melding m join melding_status ms on m.id = ms.id where (ms.status->>'type' = 'Klar') or (ms.status->>'type' = 'Retry' and (ms.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for update of m skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            OmsorgsarbeidMessageRowMapper()
        ).singleOrNull()
    }

    internal class OmsorgsarbeidMessageRowMapper : RowMapper<PersistertKafkaMelding> {
        override fun mapRow(rs: ResultSet, rowNum: Int): PersistertKafkaMelding {
            return PersistertKafkaMelding(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                melding = rs.getString("melding"),
                correlationId = rs.getString("correlation_id"),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
            )
        }
    }
}
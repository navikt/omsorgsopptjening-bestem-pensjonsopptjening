package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Status
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.ResultSet
import java.util.UUID

class BehandlingRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {

    fun persist(behandling: Behandling): FullførtBehandling {
        return behandling.toDb().let { obj ->
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update(
                """insert into behandling (omsorgs_ar, omsorgsyter, omsorgsmottaker, omsorgstype, grunnlag, vilkarsvurdering, utfall, kafkaMeldingId, status, statushistorikk) values (:omsorgsar, :omsorgsyter, :omsorgsmottaker, :omsorgstype, to_jsonb(:grunnlag::jsonb), to_jsonb(:vilkarsvurdering::jsonb), to_jsonb(:utfall::jsonb), :kafkaMeldingId, :status, to_jsonb(:statushistorikk::jsonb))""",
                MapSqlParameterSource(
                    mapOf(
                        "omsorgsar" to obj.omsorgsAr,
                        "omsorgsyter" to obj.omsorgsyter,
                        "omsorgsmottaker" to obj.omsorgsmottaker,
                        "omsorgstype" to obj.omsorgstype.toString(),
                        "grunnlag" to obj.grunnlag.mapToJson(),
                        "vilkarsvurdering" to obj.vilkårsvurdering.mapToJson(),
                        "utfall" to obj.utfall.mapToJson(),
                        "kafkaMeldingId" to obj.meldingId,
                        "status" to obj.status.databaseName(),
                        "statushistorikk" to obj.statushistorikk.serializeList()
                    ),
                ),
                keyHolder
            )
            finn(keyHolder.keys!!["id"] as UUID)
        }
    }

    fun updateStatus(behandling: FullførtBehandling) {
        jdbcTemplate.update(
            """update behandling
                | set status = :status,
                | statushistorikk = to_jsonb(:statushistorikk::jsonb)
                | where id = :id""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to behandling.id,
                    "status" to behandling.status.databaseName(),
                    "statushistorikk" to behandling.statushistorikk.serializeList(),
                ),
            ),
        )
    }

    fun finn(id: UUID): FullførtBehandling {
        return jdbcTemplate.query(
            """select * from behandling where id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            BehandlingRowMapper()
        ).single().toDomain()
    }

    fun finnForMelding(meldingId: UUID): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where kafkameldingId = :meldingId""",
            mapOf("meldingId" to meldingId),
            BehandlingRowMapper()
        ).toList().toDomain()
    }

    fun finnForOmsorgsyter(fnr: String): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsyter = :omsorgsyter""",
            mapOf<String, Any>(
                "omsorgsyter" to fnr
            ),
            BehandlingRowMapper()
        ).toDomain()
    }

    fun finnForOmsorgsyterOgAr(fnr: String, ar: Int): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsyter = :omsorgsyter and omsorgs_ar = :ar 
            """.trimMargin(),
            mapOf<String, Any>(
                "omsorgsyter" to fnr,
                "ar" to ar
            ),
            BehandlingRowMapper()
        ).toDomain()
    }

    fun finnForOmsorgsmottakerOgAr(omsorgsmottaker: String, ar: Int): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsmottaker = :omsorgsmottaker and omsorgs_ar = :ar
            """.trimMargin(),
            mapOf<String, Any>(
                "omsorgsmottaker" to omsorgsmottaker,
                "ar" to ar

            ),
            BehandlingRowMapper()
        ).toDomain()
    }

    fun finnForOmsorgsytersAndreBarn(
        omsorgsyter: String,
        ar: Int,
        andreBarnEnnOmsorgsmottaker: List<String>
    ): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsyter <> :omsorgsyter and omsorgs_ar = :ar and omsorgsmottaker in (:andrebarn)""",
            mapOf(
                "omsorgsyter" to omsorgsyter,
                "ar" to ar,
                "andrebarn" to andreBarnEnnOmsorgsmottaker.ifEmpty { "('')" }
            ),
            BehandlingRowMapper()
        ).toDomain()
    }
}

internal class BehandlingRowMapper : RowMapper<BehandlingDb> {
    override fun mapRow(rs: ResultSet, rowNum: Int): BehandlingDb {
        return BehandlingDb(
            id = UUID.fromString(rs.getString("id")),
            opprettet = rs.getTimestamp("opprettet").toInstant(),
            omsorgsAr = rs.getInt("omsorgs_ar"),
            omsorgsyter = rs.getString("omsorgsyter"),
            omsorgsmottaker = rs.getString("omsorgsmottaker"),
            omsorgstype = OmsorgskategoriDb.valueOf(rs.getString("omsorgstype")),
            grunnlag = deserialize(rs.getString("grunnlag")),
            vilkårsvurdering = deserialize(rs.getString("vilkarsvurdering")),
            utfall = deserialize(rs.getString("utfall")),
            meldingId = UUID.fromString(rs.getString("kafkaMeldingId")),
            statushistorikk = rs.getString("statushistorikk").deserializeList()
        )
    }
}

private fun Status.databaseName(): String {
    return when (this) {
        is Status.Vilkårsvurdert -> "Vilkårsvurdert"
        is Status.Stoppet -> "Stoppet"
    }
}
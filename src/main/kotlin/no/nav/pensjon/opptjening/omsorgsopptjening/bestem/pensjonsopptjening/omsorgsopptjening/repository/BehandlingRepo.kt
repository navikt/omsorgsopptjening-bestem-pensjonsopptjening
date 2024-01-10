package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.*

@Component
class BehandlingRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {

    fun persist(behandling: Behandling): FullførtBehandling {
        return behandling.toDb().let { obj ->
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update(
                """insert into behandling (omsorgs_ar, omsorgsyter, omsorgsmottaker, omsorgstype, grunnlag, vilkarsvurdering, utfall, kafkaMeldingId) values (:omsorgsar, :omsorgsyter, :omsorgsmottaker, :omsorgstype, to_jsonb(:grunnlag::jsonb), to_jsonb(:vilkarsvurdering::jsonb), to_jsonb(:utfall::jsonb), :kafkaMeldingId)""",
                MapSqlParameterSource(
                    mapOf(
                        "omsorgsar" to obj.omsorgsAr,
                        "omsorgsyter" to obj.omsorgsyter,
                        "omsorgsmottaker" to obj.omsorgsmottaker,
                        "omsorgstype" to obj.omsorgstype.toString(),
                        "grunnlag" to obj.grunnlag.mapToJson(),
                        "vilkarsvurdering" to obj.vilkårsvurdering.mapToJson(),
                        "utfall" to obj.utfall.mapToJson(),
                        "kafkaMeldingId" to obj.meldingId
                    ),
                ),
                keyHolder
            )
            finn(keyHolder.keys!!["id"] as UUID)
        }
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

    fun finnForOmsorgsyter(fnr: String): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsyter = :omsorgsyter and (stoppet = false or stoppet is null)""",
            mapOf<String, Any>(
                "omsorgsyter" to fnr
            ),
            BehandlingRowMapper()
        ).toDomain()
    }

    fun finnForOmsorgsyterOgAr(fnr: String, ar: Int): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsyter = :omsorgsyter and omsorgs_ar = :ar and (stoppet = false or stoppet is null) 
            """.trimMargin(),
            //   and stoppet <> true
            mapOf<String, Any>(
                "omsorgsyter" to fnr,
                "ar" to ar
            ),
            BehandlingRowMapper()
        ).toDomain()
    }

    // TODO: gjøre dette penere
    fun stoppBehandlingerForMelding(meldingsId: UUID) {
        jdbcTemplate.update(
            """update behandling set stoppet = true where kafkaMeldingId = :meldingsId""",
            mapOf<String, Any>(
                "meldingsId" to meldingsId
            )
        )
    }

    fun finnForOmsorgsmottakerOgAr(omsorgsmottaker: String, ar: Int): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsmottaker = :omsorgsmottaker and omsorgs_ar = :ar
                | and (stoppet = false or stoppet is null)
            """.trimMargin(),
            mapOf<String, Any>(
                "omsorgsmottaker" to omsorgsmottaker,
                "ar" to ar

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
            omsorgstype = OmsorgstypeDb.valueOf(rs.getString("omsorgstype")),
            grunnlag = deserialize(rs.getString("grunnlag")),
            vilkårsvurdering = deserialize(rs.getString("vilkarsvurdering")),
            utfall = deserialize(rs.getString("utfall")),
            meldingId = UUID.fromString(rs.getString("kafkaMeldingId"))
        )
    }
}
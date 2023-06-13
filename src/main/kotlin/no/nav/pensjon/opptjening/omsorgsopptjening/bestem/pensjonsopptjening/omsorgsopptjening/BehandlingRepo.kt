package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import jakarta.transaction.Transactional
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingsutfallDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BeriketGrunnlagDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BeriketOmsorgsgrunnlagDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.OmsorgstypeDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.VilkårsvurderingDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDomain
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class BehandlingRepo {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Transactional
    fun persist(behandling: Behandling): FullførtBehandling {
        return behandling.toDb().let { obj ->
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update(
                """insert into behandling (omsorgs_ar, omsorgsyter, omsorgsmottaker, omsorgstype, grunnlag, vilkarsvurdering, utfall) values (:omsorgsar, :omsorgsyter, :omsorgsmottaker, :omsorgstype, to_json(:grunnlag::json), to_json(:vilkarsvurdering::json), to_json(:utfall::json))""",
                MapSqlParameterSource(
                    mapOf<String, Any>(
                        "omsorgsar" to obj.omsorgsAr,
                        "omsorgsyter" to obj.omsorgsyter,
                        "omsorgsmottaker" to obj.omsorgsmottaker,
                        "omsorgstype" to obj.omsorgstype.toString(),
                        "grunnlag" to obj.grunnlag.mapToJson(),
                        "vilkarsvurdering" to obj.vilkårsvurdering.mapToJson(),
                        "utfall" to obj.utfall.mapToJson(),
                    ),
                ),
                keyHolder
            )
            find(keyHolder.keys!!["id"] as Long)
        }
    }

    private fun find(id: Long): FullførtBehandling {
        return jdbcTemplate.query(
            """select * from behandling where id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            VilkårsresultatRowMapper()
        ).single().toDomain()
    }

    fun findAll(fnr: String): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from behandling where omsorgsyter = :omsorgsyter""",
            mapOf<String, Any>(
                "omsorgsyter" to fnr
            ),
            VilkårsresultatRowMapper()
        ).toDomain()
    }
}

internal class VilkårsresultatRowMapper : RowMapper<BehandlingDb> {
    override fun mapRow(rs: ResultSet, rowNum: Int): BehandlingDb {
        return BehandlingDb(
            id = rs.getLong("id"),
            omsorgsAr = rs.getInt("omsorgs_ar"),
            omsorgsyter = rs.getString("omsorgsyter"),
            omsorgsmottaker = rs.getString("omsorgsmottaker"),
            omsorgstype = OmsorgstypeDb.valueOf(rs.getString("omsorgstype")),
            grunnlag = rs.getString("grunnlag").mapToClass(BeriketGrunnlagDb::class.java),
            vilkårsvurdering = rs.getString("vilkarsvurdering").mapToClass(VilkårsvurderingDb::class.java),
            utfall = rs.getString("utfall").mapToClass(BehandlingsutfallDb::class.java)
        )
    }
}


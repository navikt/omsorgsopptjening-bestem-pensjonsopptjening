package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRowMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDomain
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.Clock
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class KontrollbehandlingRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock,
) {

    private fun KontrollbehandlingStatus.databaseName(): String {
        return when (this) {
            is KontrollbehandlingStatus.Feilet -> "Feilet"
            is KontrollbehandlingStatus.Ferdig -> "Ferdig"
            is KontrollbehandlingStatus.Klar -> "Klar"
            is KontrollbehandlingStatus.Retry -> "Retry"
        }
    }

    private fun KontrollbehandlingStatus.karanteneTilString(): String? {
        return when (val s = this) {
            is KontrollbehandlingStatus.Retry -> s.karanteneTil.toString()
            else -> null
        }
    }

    fun bestillKontroll(innlesingId: InnlesingId, referanse: String) {
        val status = KontrollbehandlingStatus.Klar()
        jdbcTemplate.update(
            """
                with original as (
                    select b.id, b.kafkameldingid from behandling b
                    join melding m on m.id = b.kafkameldingid
                    where m.innlesing_id = :innlesingid                                                      
                )
                insert into kontrollbehandling (status, statushistorikk, referanse, originalId, kafkameldingid)
                select :status, to_jsonb(:statushistorikk::jsonb), :referanse, id, kafkameldingid from original                                       
            """.trimIndent(),
            MapSqlParameterSource(
                mapOf(
                    "innlesingid" to innlesingId.toString(),
                    "status" to status.databaseName(),
                    "statushistorikk" to listOf(status).serializeList(),
                    "referanse" to referanse,
                )
            )
        )
    }

    fun updateStatus(kontrollRad: Kontrollbehandling) {
        jdbcTemplate.update(
            """update kontrollbehandling set status = :status, karantene_til = :karanteneTil::timestamptz, statushistorikk = to_jsonb(:statushistorikk::jsonb) where kontrollId = :kontrollId""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "kontrollId" to kontrollRad.kontrollId,
                    "status" to kontrollRad.status.databaseName(),
                    "karanteneTil" to kontrollRad.status.karanteneTilString(),
                    "statushistorikk" to kontrollRad.statushistorikk.serializeList()
                ),
            ),
        )
    }

    fun find(kontrollId: UUID): Kontrollbehandling {
        return jdbcTemplate.query(
            """select * 
                |from kontrollbehandling 
                |where kontrollId = :kontrollId""".trimMargin(),
            mapOf<String, Any>(
                "kontrollId" to kontrollId
            ),
            KontrollRadMapper()
        ).single()
    }


    fun finnNesteMeldingerForBehandling(antall: Int): Locked {
        val lockId = UUID.randomUUID()
        val meldinger = finnNesteKlarTilProsessering(lockId, antall).ifEmpty { finnNesteKlarForRetry(lockId, antall) }
        return Locked(lockId, meldinger.map { find(it) })
    }

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */
    fun finnNesteKlarTilProsessering(lockId: UUID, antall: Int): List<UUID> {
        val now = Instant.now(clock).toString()

        jdbcTemplate.update(
            """update kontrollbehandling set lockId = :lockId, lockTime = :now::timestamptz
             | where kontrollId in (
             |   select kontrollId 
             |   from kontrollbehandling
             |   where status = 'Klar'
             |   and lockId is null
             |   order by kontrollId
             |   fetch first :antall rows only
             |   for update skip locked)""".trimMargin(),
            mapOf(
                "now" to now,
                "antall" to antall,
                "lockId" to lockId,
            )
        )

        return jdbcTemplate.queryForList(
            """select kontrollId 
             | from kontrollbehandling
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
            """update kontrollbehandling set lockId = :lockId, lockTime = :now::timestamptz
             | where kontrollId in (
             |   select kontrollId 
             |   from kontrollbehandling
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
            """select kontrollId 
             | from kontrollbehandling
             | where lockId = :lockId""".trimMargin(),
            mapOf(
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }

    fun frigi(locked: Locked) {
        jdbcTemplate.update(
            """update kontrollbehandling set lockId = null where lockId = :lockId""",
            mapOf(
                "lockId" to locked.lockId,
            ),
        )
    }

    fun frigiGamleLåser(): Int {
        val oneHourAgo = Instant.now(clock).minus(1.hours.toJavaDuration()).toString()
        return jdbcTemplate.update(
            """update kontrollbehandling set lockId = null, lockTime = null 
            |where lockId is not null and lockTime < :oneHourAgo::timestamptz""".trimMargin(),
            mapOf<String, Any>(
                "oneHourAgo" to oneHourAgo
            )
        )
    }

    fun update(kontrollRad: Kontrollbehandling, behandling: Behandling): FullførtBehandling {
        return behandling.toDb().let { obj ->
            jdbcTemplate.update(
                """update kontrollbehandling set
                    |id = uuid_generate_v4(),
                    |omsorgs_ar = :omsorgsar,
                    |omsorgsyter = :omsorgsyter,
                    |omsorgsmottaker = :omsorgsmottaker,
                    |omsorgstype = :omsorgstype,
                    |grunnlag = to_jsonb(:grunnlag::jsonb),
                    |vilkarsvurdering = to_jsonb(:vilkarsvurdering::jsonb),
                    |utfall = to_jsonb(:utfall::jsonb)
                    |where kontrollId = :kontrollId
                """.trimMargin(),
                MapSqlParameterSource(
                    mapOf(
                        "kontrollId" to kontrollRad.kontrollId,
                        "omsorgsar" to obj.omsorgsAr,
                        "omsorgsyter" to obj.omsorgsyter,
                        "omsorgsmottaker" to obj.omsorgsmottaker,
                        "omsorgstype" to obj.omsorgstype.toString(),
                        "grunnlag" to obj.grunnlag.mapToJson(),
                        "vilkarsvurdering" to obj.vilkårsvurdering.mapToJson(),
                        "utfall" to obj.utfall.mapToJson()
                    ),
                ),
            )
            finnForKontrollId(kontrollRad.kontrollId)
        }
    }

    fun finnForKontrollId(kontrollId: UUID): FullførtBehandling {
        return jdbcTemplate.query(
            """select * from kontrollbehandling where kontrollId = :id""",
            mapOf<String, Any>(
                "id" to kontrollId
            ),
            BehandlingRowMapper()
        ).single().toDomain()
    }

    fun finnForOmsorgsyterOgAr(fnr: String, ar: Int, referanse: String): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from kontrollbehandling where omsorgsyter = :omsorgsyter and omsorgs_ar = :ar and referanse = :referanse""",
            mapOf<String, Any>(
                "omsorgsyter" to fnr,
                "ar" to ar,
                "referanse" to referanse,
            ),
            BehandlingRowMapper()
        ).toDomain()
    }

    fun finnForOmsorgsmottakerOgAr(omsorgsmottaker: String, ar: Int, referanse: String): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from kontrollbehandling where omsorgsmottaker = :omsorgsmottaker and omsorgs_ar = :ar and referanse = :referanse""",
            mapOf<String, Any>(
                "omsorgsmottaker" to omsorgsmottaker,
                "ar" to ar,
                "referanse" to referanse,
            ),
            BehandlingRowMapper()
        ).toDomain()
    }


    internal class KontrollRadMapper : RowMapper<Kontrollbehandling> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Kontrollbehandling {
            return Kontrollbehandling(
                kontrollId = UUID.fromString(rs.getString("kontrollId")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                orginalBehandlingId = UUID.fromString(rs.getString("originalId")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
                referanse = rs.getString("referanse")
            )
        }
    }

    data class Locked(val lockId: UUID, val rader: List<Kontrollbehandling>)
}
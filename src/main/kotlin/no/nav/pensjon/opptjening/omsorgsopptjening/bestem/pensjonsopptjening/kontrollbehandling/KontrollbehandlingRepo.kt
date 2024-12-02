package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Brevopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRowMapper
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDb
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.toDomain
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.deserializeList
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToJson
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serializeList
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
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

    fun bestillKontroll(innlesingId: InnlesingId, referanse: String, år: Int) {
        val status = KontrollbehandlingStatus.Klar()
        jdbcTemplate.update(
            """
                with original as (
                    select m.id from melding m                   
                    where m.innlesing_id = :innlesingid                                         
                )
                insert into kontrollmelding (status, statushistorikk, referanse, kafkameldingid, omsorgs_ar)
                select :status, to_jsonb(:statushistorikk::jsonb), :referanse, id, :omsorgs_ar from original                                       
            """.trimIndent(),
            MapSqlParameterSource(
                mapOf(
                    "innlesingid" to innlesingId.toString(),
                    "status" to status.databaseName(),
                    "statushistorikk" to listOf(status).serializeList(),
                    "referanse" to referanse,
                    "omsorgs_ar" to år,
                )
            ),
        )
    }

    fun updateStatus(kontrollRad: Kontrollbehandling) {
        jdbcTemplate.update(
            """update kontrollmelding set status = :status, karantene_til = :karanteneTil::timestamptz, statushistorikk = to_jsonb(:statushistorikk::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to kontrollRad.kontrollId,
                    "status" to kontrollRad.status.databaseName(),
                    "karanteneTil" to kontrollRad.status.karanteneTilString(),
                    "statushistorikk" to kontrollRad.statushistorikk.serializeList()
                ),
            ),
        )
    }

    fun find(id: UUID): Kontrollbehandling {
        return jdbcTemplate.query(
            """select * 
                |from kontrollmelding 
                |where id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            KontrollbehandlingRowMapper()
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
            """update kontrollmelding set lockId = :lockId, lockTime = :now::timestamptz
             | where id in (
             |   select id 
             |   from kontrollmelding
             |   where status = 'Klar'
             |   and lockId is null
             |   order by id
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
             | from kontrollmelding
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
            """update kontrollmelding set lockId = :lockId, lockTime = :now::timestamptz
             | where id in (
             |   select id 
             |   from kontrollmelding
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
             | from kontrollmelding
             | where lockId = :lockId""".trimMargin(),
            mapOf(
                "lockId" to lockId,
            ),
            UUID::class.java
        )
    }

    fun frigi(locked: Locked) {
        jdbcTemplate.update(
            """update kontrollmelding set lockId = null, lockTime = null where lockId = :lockId""",
            mapOf(
                "lockId" to locked.lockId,
            ),
        )
    }

    fun frigiGamleLåser(): Int {
        val oneHourAgo = Instant.now(clock).minus(1.hours.toJavaDuration()).toString()
        return jdbcTemplate.update(
            """update kontrollmelding set lockId = null, lockTime = null 
            |where lockId is not null and lockTime < :oneHourAgo::timestamptz""".trimMargin(),
            mapOf<String, Any>(
                "oneHourAgo" to oneHourAgo
            )
        )
    }

    fun persist(kontrollRad: Kontrollbehandling, behandling: Behandling): FullførtBehandling {
        return behandling.toDb().let { obj ->
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update(
                """insert into kontrollbehandling (omsorgs_ar, omsorgsyter, omsorgsmottaker, omsorgstype, grunnlag, vilkarsvurdering, utfall, kafkaMeldingId, referanse) values (:omsorgsar, :omsorgsyter, :omsorgsmottaker, :omsorgstype, to_jsonb(:grunnlag::jsonb), to_jsonb(:vilkarsvurdering::jsonb), to_jsonb(:utfall::jsonb), :kafkaMeldingId, :referanse)""",
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
                        "referanse" to kontrollRad.referanse,
                    ),
                ),
                keyHolder
            )
            findBehandling(keyHolder.keys!!["id"] as UUID)
        }
    }

    fun oppdaterMedGodskriv(fullførtBehandling: FullførtBehandling, godskrivOpptjening: GodskrivOpptjening.Transient) {
        jdbcTemplate.update(
            """update kontrollbehandling set godskriv = to_jsonb(:godskriv::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf(
                    "id" to fullførtBehandling.id,
                    "godskriv" to serialize(godskrivOpptjening)
                )
            )
        )
    }

    fun oppdaterMedBrev(
        fullførtBehandling: FullførtBehandling,
        brevopplysninger: Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker
    ) {
        jdbcTemplate.update(
            """update kontrollbehandling set brev = to_jsonb(:brev::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf(
                    "id" to fullførtBehandling.id,
                    "brev" to serialize(brevopplysninger)
                )
            )
        )
    }

    fun oppdaterMedOppgave(fullførtBehandling: FullførtBehandling, oppgaveopplysninger: List<Oppgaveopplysninger>) {
        jdbcTemplate.update(
            """update kontrollbehandling set oppgave = to_jsonb(:oppgave::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf(
                    "id" to fullførtBehandling.id,
                    "oppgave" to serialize(oppgaveopplysninger)
                )
            )
        )
    }

    fun findBehandling(behandlingId: UUID): FullførtBehandling {
        return jdbcTemplate.query(
            """select * 
                |from kontrollbehandling 
                |where id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to behandlingId
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

    fun finnForOmsorgsytersAndreBarn(
        omsorgsyter: String,
        ar: Int,
        andreBarnEnnOmsorgsmottaker: List<String>,
        referanse: String,
    ): List<FullførtBehandling> {
        return jdbcTemplate.query(
            """select * from kontrollbehandling where omsorgsyter <> :omsorgsyter and omsorgs_ar = :ar and omsorgsmottaker in (:andrebarn) and referanse = :referanse""",
            mapOf(
                "omsorgsyter" to omsorgsyter,
                "ar" to ar,
                "andrebarn" to andreBarnEnnOmsorgsmottaker.ifEmpty { "('')" },
                "referanse" to referanse,
            ),
            BehandlingRowMapper()
        ).toDomain()
    }


    internal class KontrollbehandlingRowMapper : RowMapper<Kontrollbehandling> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Kontrollbehandling {
            return Kontrollbehandling(
                kontrollId = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                kontrollmeldingId = UUID.fromString(rs.getString("id")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
                referanse = rs.getString("referanse"),
                omsorgsÅr = rs.getInt("omsorgs_ar"),
                kafkameldingid = UUID.fromString(rs.getString("kafkameldingid"))
            )
        }
    }

    data class Locked(val lockId: UUID, val rader: List<Kontrollbehandling>)
}
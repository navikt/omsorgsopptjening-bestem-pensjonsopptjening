package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BrevÅrsak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
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

@Component
class BrevRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val clock: Clock = Clock.systemUTC()
) {

    fun persist(brev: Brev.Transient): Brev.Persistent {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update(
            """insert into brev (behandlingId, årsak, status, karantene_til, statushistorikk) 
                |values (:behandlingId, :arsak, :status, :karantene_til::timestamptz, to_jsonb(:statushistorikk::jsonb))""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "behandlingId" to brev.behandlingId,
                    "arsak" to brev.årsak.toDb(),
                    "statushistorikk" to brev.statushistorikk.serializeList(),
                    "status" to when (val s = brev.status) {
                        is Brev.Status.Feilet -> "Feilet"
                        is Brev.Status.Ferdig -> "Ferdig"
                        is Brev.Status.Klar -> "Klar"
                        is Brev.Status.Retry -> "Retry"
                    },
                    "karantene_til" to when(val s = brev.status) {
                        is Brev.Status.Retry -> s.karanteneTil.toString()
                        else -> null
                    }
                ),
            ),
            keyHolder
        )

        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(brev: Brev.Persistent) {
        jdbcTemplate.update(
            """update brev set 
                | statushistorikk = to_jsonb(:statushistorikk::jsonb),
                | status = :status,
                | karantene_til = :karantene_til::timestamptz
                | where id = :id""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "id" to brev.id,
                    "statushistorikk" to brev.statushistorikk.serializeList(),
                    "status" to when (val s = brev.status) {
                        is Brev.Status.Feilet -> "Feilet"
                        is Brev.Status.Ferdig -> "Ferdig"
                        is Brev.Status.Klar -> "Klar"
                        is Brev.Status.Retry -> "Retry"
                    },
                    "karantene_til" to when(val s = brev.status) {
                        is Brev.Status.Retry -> s.karanteneTil.toString()
                        else -> null
                    }
                ),
            ),
        )
    }

    fun find(id: UUID): Brev.Persistent {
        return jdbcTemplate.query(
            """select o.*, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar  
                |from brev o 
                |join behandling b on b.id = o.behandlingId 
                |join melding m on m.id = b.kafkaMeldingId 
                |where o.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            BrevMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<Brev.Persistent> {
        return jdbcTemplate.query(
            """select o.*, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar  
                |from brev o 
                |join behandling b on b.id = o.behandlingId 
                |join melding m on m.id = b.kafkaMeldingId 
                |where m.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            BrevMapper()
        )
    }

    fun findForBehandling(id: UUID): List<Brev.Persistent> {
        return jdbcTemplate.query(
            """select o.*, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar
                | from brev o
                | join behandling b on b.id = o.behandlingId
                | join melding m on m.id = b.kafkaMeldingId 
                | where b.id = :id""".trimMargin(),
            mapOf<String, Any>(
                "id" to id
            ),
            BrevMapper()
        )
    }

    /**
     * Utformet for å være mekanismen som tilrettelegger for at flere podder kan prosessere data i paralell.
     * "select for update skip locked" sørger for at raden som leses av en connection (pod) ikke vil plukkes opp av en
     * annen connection (pod) så lenge transaksjonen lever.
     */

    fun finnNesteUprosesserte(antall: Int): List<Brev.Persistent> {
        val nesteUprosesserte = finnNesteUprosesserteKlar(antall)
            .ifEmpty { finnNesteUprosesserteRetry(antall) }
        return nesteUprosesserte.map { find(it) }
    }

    fun finnNesteUprosesserteKlar(antall: Int): List<UUID> {
        return jdbcTemplate.queryForList(
            """select id 
                | from brev
                | where status = 'Klar'
                | order by id
                | fetch first :antall rows only for no key update skip locked""".trimMargin(),
            mapOf(
                "now" to Instant.now(clock).toString(),
                "antall" to antall,
            ),
            UUID::class.java
        )
    }

    fun finnNesteUprosesserteRetry(antall: Int): List<UUID> {
        return jdbcTemplate.queryForList(
            """select id
                | from brev
                | where status = 'Retry'
                |   and karantene_til < (:now)::timestamptz
                |   and karantene_til is not null
                | order by karantene_til asc
                | fetch first :antall rows only for no key update skip locked""".trimMargin(),
            mapOf(
                "now" to Instant.now(clock).toString(),
                "antall" to antall,
            ),
            UUID::class.java
        )
    }

    private fun BrevÅrsak.toDb(): String {
        return when (this) {
            BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR -> BrevÅrsakDb.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR
            BrevÅrsak.OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER -> BrevÅrsakDb.OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER
            BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG -> BrevÅrsakDb.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG
            BrevÅrsak.FORELDRE_ER_UKJENT -> BrevÅrsakDb.FORELDRE_ER_UKJENT
        }.toString()
    }

    private enum class BrevÅrsakDb {
        OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR,
        OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER,
        ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG,
        FORELDRE_ER_UKJENT
    }


    internal class BrevMapper : RowMapper<Brev.Persistent> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Brev.Persistent {
            return Brev.Persistent(
                id = UUID.fromString(rs.getString("id")),
                opprettet = rs.getTimestamp("opprettet").toInstant(),
                omsorgsyter = rs.getString("omsorgsyter"),
                behandlingId = rs.getString("behandlingId").let { UUID.fromString(it) },
                meldingId = UUID.fromString(rs.getString("meldingid")),
                correlationId = CorrelationId.fromString(rs.getString("correlation_id")),
                statushistorikk = rs.getString("statushistorikk").deserializeList(),
                innlesingId = InnlesingId.fromString(rs.getString("innlesing_id")),
                omsorgsår = rs.getInt("omsorgs_ar"),
                årsak = rs.getString("årsak").let { BrevÅrsakDb.valueOf(it).toDomain() }
            )
        }

        private fun BrevÅrsakDb.toDomain(): BrevÅrsak {
            return when (this) {
                BrevÅrsakDb.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR -> BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR
                BrevÅrsakDb.OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER -> BrevÅrsak.OMSORGSYTER_IKKE_FORELDER_AV_OMSORGSMOTTAKER
                BrevÅrsakDb.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG -> BrevÅrsak.ANNEN_FORELDER_HAR_LAVERE_PENSJONSPOENG
                BrevÅrsakDb.FORELDRE_ER_UKJENT -> BrevÅrsak.FORELDRE_ER_UKJENT
            }
        }
    }
}
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
            """insert into brev (behandlingId, årsak) values (:behandlingId, :arsak)""",
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "behandlingId" to brev.behandlingId,
                    "arsak" to brev.årsak.toDb()
                ),
            ),
            keyHolder
        )
        jdbcTemplate.update(
            """insert into brev_status (id, status, statushistorikk) values (:id, to_jsonb(:status::jsonb), to_jsonb(:statushistorikk::jsonb))""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to keyHolder.keys!!["id"] as UUID,
                    "status" to serialize(brev.status),
                    "statushistorikk" to brev.statushistorikk.serializeList()
                ),
            ),
        )
        return find(keyHolder.keys!!["id"] as UUID)
    }

    fun updateStatus(brev: Brev.Persistent) {
        jdbcTemplate.update(
            """update brev_status set status = to_jsonb(:status::jsonb), statushistorikk = to_jsonb(:statushistorikk::jsonb) where id = :id""",
            MapSqlParameterSource(
                mapOf<String, Any>(
                    "id" to brev.id,
                    "status" to serialize(brev.status),
                    "statushistorikk" to brev.statushistorikk.serializeList()
                ),
            ),
        )
    }

    fun find(id: UUID): Brev.Persistent {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar  from brev o join brev_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where o.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            BrevMapper()
        ).single()
    }

    fun findForMelding(id: UUID): List<Brev.Persistent> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar  from brev o join brev_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where m.id = :id""",
            mapOf<String, Any>(
                "id" to id
            ),
            BrevMapper()
        )
    }

    fun findForBehandling(id: UUID): List<Brev.Persistent> {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk,  m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar  from brev o join brev_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId where b.id = :id""",
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
    fun finnNesteUprosesserte(): Brev.Persistent? {
        return jdbcTemplate.query(
            """select o.*, os.statushistorikk, m.id as meldingid, m.correlation_id, m.innlesing_id, b.omsorgsyter, b.omsorgs_ar from brev o join brev_status os on o.id = os.id join behandling b on b.id = o.behandlingId join melding m on m.id = b.kafkaMeldingId  where (os.status->>'type' = 'Klar') or (os.status->>'type' = 'Retry' and (os.status->>'karanteneTil')::timestamptz < (:now)::timestamptz) fetch first row only for no key update of o skip locked""",
            mapOf(
                "now" to Instant.now(clock).toString()
            ),
            BrevMapper()
        ).singleOrNull()
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
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class EndringsloggRepo(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun lagre(endring: IntensjonInsert): UUID {
        return requireNotNull(jdbcTemplate.queryForObject(
            """insert into endringslogg (
                | endret_tidspunkt,
                | endret_av,
                | kilde,
                | handling,
                | begrunnelse,
                | status,
                | input_id,
                | input
                |) values (
                | :endret_tidspunkt::timestamptz,
                | :endret_av,
                | :kilde,
                | :handling,
                | :begrunnelse,
                | :status,
                | :input_id,
                | to_jsonb(:input::jsonb)
                |)
                |returning id""".trimMargin(),
            MapSqlParameterSource(
                mapOf<String, Any?>(
                    "endret_tidspunkt" to endring.endretTidspunkt.toString(),
                    "endret_av" to endring.endretAv,
                    "kilde" to endring.kilde.dbValue,
                    "handling" to endring.handling.dbValue,
                    "begrunnelse" to endring.begrunnelse,
                    "status" to endring.status.dbValue,
                    "input_id" to endring.input.inputId(),
                    "input" to serialize(endring.input),
                ),
            ),
            UUID::class.java
        ))
    }

    fun oppdaterResultat(resultatUpdate: ResultatUpdate) {
        val (status, resultatId, detalj) = resultatUpdate.resultat

        val antallOppdaterteRader = jdbcTemplate.update(
            """update endringslogg
                |set endret_tidspunkt = :endret_tidspunkt::timestamptz,
                |    status = :status,
                |    resultat_id = :resultat_id,
                |    detalj = :detalj
                |where id = :id""".trimMargin(),
            mapOf(
                "id" to resultatUpdate.loggId,
                "endret_tidspunkt" to resultatUpdate.endretTidspunkt.toString(),
                "status" to status.dbValue,
                "resultat_id" to resultatId,
                "detalj" to detalj,
            ),
        )

        check(antallOppdaterteRader == 1) {
            "Forventet å oppdatere 1 rad i endringslogg for id=${resultatUpdate.loggId}, men oppdaterte $antallOppdaterteRader"
        }
    }

    data class IntensjonInsert(
        val endretAv: String,
        val kilde: Endringslogg.Kilde,
        val handling: Endringslogg.Handling,
        val status: Endringslogg.Status,
        val input: Endringslogg.Input,
        val begrunnelse: String? = null,
        val endretTidspunkt: Instant = Instant.now(),
    )

    data class ResultatUpdate(
        val loggId: UUID,
        val resultat: Endringslogg.Resultat,
        val endretTidspunkt: Instant = Instant.now(),
    )
}

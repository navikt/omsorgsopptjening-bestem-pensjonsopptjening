package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.Endringslogg
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.EndringsloggRepo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Instant
import java.util.UUID

class EndringsloggRepoTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var endringsloggRepo: EndringsloggRepo

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Test
    fun `kan lagre og hente endringer for et mål`() {
        val meldingId1 = insertMelding()
        val meldingId2 = insertMelding()

        endringsloggRepo.lagre(
            EndringsloggRepo.IntensjonInsert(
                endretTidspunkt = Instant.parse("2026-01-01T10:00:00Z"),
                endretAv = "Z123456",
                kilde = Endringslogg.Kilde.ADMIN_WEB_API,
                handling = Endringslogg.Handling.STOPP_MELDINGER,
                status = Endringslogg.Status.STOPPET,
                input = Endringslogg.Input.Melding(meldingId = meldingId1),
                begrunnelse = "første hendelse",
            )
        )

        endringsloggRepo.lagre(
            EndringsloggRepo.IntensjonInsert(
                endretTidspunkt = Instant.parse("2026-01-01T11:00:00Z"),
                endretAv = "Z123456",
                kilde = Endringslogg.Kilde.ADMIN_WEB_API,
                handling = Endringslogg.Handling.STOPP_MELDINGER,
                status = Endringslogg.Status.FEILET,
                input = Endringslogg.Input.Melding(meldingId = meldingId2),
            )
        )

        val endringer = jdbcTemplate.finnEndringsloggForHandling(Endringslogg.Handling.STOPP_MELDINGER)

        assertThat(endringer).hasSize(2)
        assertThat(endringer.map { it.handling }).containsExactly(Endringslogg.Handling.STOPP_MELDINGER, Endringslogg.Handling.STOPP_MELDINGER)
        assertThat(endringer.map { it.kilde }).containsOnly(Endringslogg.Kilde.ADMIN_WEB_API)
        assertThat(endringer.map { it.resultatId }).containsOnlyNulls()
        assertThat(endringer.map { it.status }).containsExactly(Endringslogg.Status.FEILET, Endringslogg.Status.STOPPET)
    }

    @Test
    fun `kan lagre og hente flat entry`() {
        val meldingId = insertMelding()
        val lagret = endringsloggRepo.lagre(
            EndringsloggRepo.IntensjonInsert(
                endretAv = "Z123456",
                kilde = Endringslogg.Kilde.ADMIN_WEB_API,
                handling = Endringslogg.Handling.STOPP_MELDINGER,
                status = Endringslogg.Status.STOPPET,
                input = Endringslogg.Input.Melding(meldingId = meldingId),
                begrunnelse = "ingen innslag",
            )
        )

        val hentet = jdbcTemplate.finnEndringslogg(lagret)

        assertThat(hentet.inputId).isEqualTo(meldingId)
        assertThat(hentet.resultatId).isNull()
        assertThat(hentet.begrunnelse).isEqualTo("ingen innslag")
        assertThat(hentet.handling).isEqualTo(Endringslogg.Handling.STOPP_MELDINGER)
    }

    @Test
    fun `kan oppdatere intent-logget med resultat`() {
        val meldingId = insertMelding()
        val lagret = endringsloggRepo.lagre(
            EndringsloggRepo.IntensjonInsert(
                endretAv = "Z123456",
                kilde = Endringslogg.Kilde.ADMIN_WEB_API,
                handling = Endringslogg.Handling.STOPP_MELDINGER,
                status = Endringslogg.Status.INTENT_LOGGET,
                input = Endringslogg.Input.Melding(meldingId = meldingId),
            )
        )

        endringsloggRepo.oppdaterResultat(
            resultatUpdate = EndringsloggRepo.ResultatUpdate(
                loggId = lagret,
                resultat = Endringslogg.Resultat(
                    status = Endringslogg.Status.OK,
                    resultatId = meldingId,
                ),
            )
        )

        val hentet = jdbcTemplate.finnEndringslogg(lagret)

        assertThat(hentet.status).isEqualTo(Endringslogg.Status.OK)
        assertThat(hentet.resultatId).isEqualTo(meldingId)
    }

    @Test
    fun `feiler dersom oppdatering av resultat ikke treffer loggrad`() {
        assertThatThrownBy {
            endringsloggRepo.oppdaterResultat(
                resultatUpdate = EndringsloggRepo.ResultatUpdate(
                    loggId = UUID.randomUUID(),
                    resultat = Endringslogg.Resultat(
                        status = Endringslogg.Status.OK,
                        resultatId = UUID.randomUUID(),
                    ),
                )
            )
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Forventet å oppdatere 1 rad i endringslogg")
    }

    private fun insertMelding(): UUID {
        val id = UUID.randomUUID()
        jdbcTemplate.update(
            """insert into melding (id, opprettet, melding, correlation_id, innlesing_id, status, statushistorikk)
                | values (:id, now(), '{}'::jsonb, :correlation_id, :innlesing_id, 'Klar', '[]'::jsonb)""".trimMargin(),
            mapOf(
                "id" to id,
                "correlation_id" to UUID.randomUUID().toString(),
                "innlesing_id" to UUID.randomUUID().toString(),
            )
        )
        return id
    }
}

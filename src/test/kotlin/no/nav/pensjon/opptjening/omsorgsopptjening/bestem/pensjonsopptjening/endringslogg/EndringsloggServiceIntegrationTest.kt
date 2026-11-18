package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.endringslogg.repository.finnEndringsloggForHandling
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID

class EndringsloggServiceIntegrationTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var endringsloggService: EndringsloggService

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Test
    fun `lagrer og oppdaterer endringslogg for alle input`() {
        val meldingId1 = insertMelding()
        val meldingId2 = insertMelding()

        val resultater = endringsloggService.kjørOperasjonerOgLogg(
            inputListe = listOf(meldingId1, meldingId2),
            loggKontekst = kontekst(),
            loggInputMapping = { Endringslogg.Input.Melding(it) },
            loggResultatMapping = { Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it) },
            operasjon = { it },
        )

        assertThat(resultater).hasSize(2)
        assertThat(resultater.map { it.second.getOrThrow() }).containsExactly(meldingId1, meldingId2)

        val endringer = jdbcTemplate.finnEndringsloggForHandling(Endringslogg.Handling.STOPP_MELDINGER)
            .filter { it.inputId == meldingId1 || it.inputId == meldingId2 }
            .associateBy { it.inputId }

        assertThat(endringer).hasSize(2)
        assertThat(endringer.getValue(meldingId1).status).isEqualTo(Endringslogg.Status.OK)
        assertThat(endringer.getValue(meldingId1).resultatId).isEqualTo(meldingId1)
        assertThat(endringer.getValue(meldingId2).status).isEqualTo(Endringslogg.Status.OK)
        assertThat(endringer.getValue(meldingId2).resultatId).isEqualTo(meldingId2)
    }

    @Test
    fun `oppdateringsfeil ruller tilbake kun den raden og resten oppdateres`() {
        val meldingIdSomFeiler = insertMelding()
        val meldingIdSomLykkes = insertMelding()

        endringsloggService.kjørOperasjonerOgLogg(
            inputListe = listOf(meldingIdSomFeiler, meldingIdSomLykkes),
            loggKontekst = kontekst(),
            loggInputMapping = { Endringslogg.Input.Melding(it) },
            loggResultatMapping = {
                if (it == meldingIdSomFeiler) {
                    error("simulert feil i resultatmapping")
                }
                Endringslogg.Resultat(status = Endringslogg.Status.OK, resultatId = it)
            },
            operasjon = { it },
        )

        val endringer = jdbcTemplate.finnEndringsloggForHandling(Endringslogg.Handling.STOPP_MELDINGER)
            .filter { it.inputId == meldingIdSomFeiler || it.inputId == meldingIdSomLykkes }
            .associateBy { it.inputId }

        assertThat(endringer).hasSize(2)
        assertThat(endringer.getValue(meldingIdSomFeiler).status).isEqualTo(Endringslogg.Status.INTENT_LOGGET)
        assertThat(endringer.getValue(meldingIdSomFeiler).resultatId).isNull()
        assertThat(endringer.getValue(meldingIdSomFeiler).detalj).isNull()

        assertThat(endringer.getValue(meldingIdSomLykkes).status).isEqualTo(Endringslogg.Status.OK)
        assertThat(endringer.getValue(meldingIdSomLykkes).resultatId).isEqualTo(meldingIdSomLykkes)
    }

    private fun kontekst(begrunnelse: String? = null): Endringslogg.Kontekst {
        return Endringslogg.Kontekst(
            kilde = Endringslogg.Kilde.ADMIN_WEB_API,
            navIdent = "srv-omsorg",
            handling = Endringslogg.Handling.STOPP_MELDINGER,
            begrunnelse = begrunnelse,
        )
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

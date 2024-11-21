package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class AggregertOppgaveForFullførteBehandlingerTest {

    private val meldingId = UUID.randomUUID()

    @Test
    fun `oppretter ingenting dersom omsorgsyter har oppgave for samme år fra før`() {
        val oppgave = AggregertOppgaveForFullførteBehandlinger(
            omsorgsyterHarOppgave = { s: String, i: Int -> true },
            omsorgsmottakerHarOppgave = { s: String, i: Int -> false },
            alleOppgaveopplysninger = FullførteBehandlingerOppgaveopplysninger(
                omsorgsyter = "omsorgsyter",
                omsorgsAr = 2024,
                behandlingOppgaveopplysninger = listOf(
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = UUID.randomUUID(),
                        omsorgsmottaker = "venenatis",
                        oppgaveopplysninger = listOf()
                    )
                ),
                meldingId = meldingId
            )
        ).oppgave

        assertThat(oppgave).isNull()
    }

    @Test
    fun `oppretter ingenting dersom omsorgsmottaker har oppgave for samme år fra før`() {
        val oppgave = AggregertOppgaveForFullførteBehandlinger(
            omsorgsyterHarOppgave = { s: String, i: Int -> false },
            omsorgsmottakerHarOppgave = { s: String, i: Int -> true },
            alleOppgaveopplysninger = FullførteBehandlingerOppgaveopplysninger(
                omsorgsyter = "omsorgsyter",
                omsorgsAr = 2024,
                behandlingOppgaveopplysninger = listOf(
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = UUID.randomUUID(),
                        omsorgsmottaker = "venenatis",
                        oppgaveopplysninger = listOf()
                    )
                ),
                meldingId = meldingId
            )
        ).oppgave

        assertThat(oppgave).isNull()
    }

    @Test
    fun `oppretter dersom verken omsorgsyter eller omsorgsmottaker har fra før`() {
        val behandlingId = UUID.randomUUID()
        val oppgave = AggregertOppgaveForFullførteBehandlinger(
            omsorgsyterHarOppgave = { s: String, i: Int -> false },
            omsorgsmottakerHarOppgave = { s: String, i: Int -> false },
            alleOppgaveopplysninger = FullførteBehandlingerOppgaveopplysninger(
                omsorgsyter = "omsorgsyter",
                omsorgsAr = 2024,
                behandlingOppgaveopplysninger = listOf(
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = behandlingId,
                        omsorgsmottaker = "venenatis",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = "omsorgsyter",
                                oppgaveTekst = "my text"
                            )
                        )
                    )
                ),
                meldingId = meldingId
            )
        ).oppgave

        assertThat(oppgave).isNotNull()
        assertThat(oppgave!!.behandlingId).isEqualTo(behandlingId)
        assertThat(oppgave.meldingId).isEqualTo(meldingId)
        assertThat(oppgave.oppgavetekst).isEqualTo(setOf("my text"))
    }

    @Test
    fun `inkluderer oppgavetekster fra alle behandlingene i samme oppgave`() {
        val oppgave = AggregertOppgaveForFullførteBehandlinger(
            omsorgsyterHarOppgave = { s: String, i: Int -> false },
            omsorgsmottakerHarOppgave = { s: String, i: Int -> false },
            alleOppgaveopplysninger = FullførteBehandlingerOppgaveopplysninger(
                omsorgsyter = "omsorgsyter",
                omsorgsAr = 2024,
                behandlingOppgaveopplysninger = listOf(
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = UUID.randomUUID(),
                        omsorgsmottaker = "sønn",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = "omsorgsyter",
                                oppgaveTekst = "tekst for sønn"
                            )
                        )
                    ),
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = UUID.randomUUID(),
                        omsorgsmottaker = "datter",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = "omsorgsyter",
                                oppgaveTekst = "tekst for datter"
                            )
                        )
                    )
                ),
                meldingId = meldingId
            )
        ).oppgave

        assertThat(oppgave).isNotNull()
        assertThat(oppgave!!.behandlingId).isNull()
        assertThat(oppgave.meldingId).isEqualTo(meldingId)
        assertThat(oppgave.oppgavetekst).isEqualTo(setOf("tekst for sønn", "tekst for datter"))
    }

    @Test
    fun `oppgaveopplysning ingen regnes ikke som en oppgave`() {
        val behandlingId = UUID.randomUUID()
        val oppgave = AggregertOppgaveForFullførteBehandlinger(
            omsorgsyterHarOppgave = { s: String, i: Int -> false },
            omsorgsmottakerHarOppgave = { s: String, i: Int -> false },
            alleOppgaveopplysninger = FullførteBehandlingerOppgaveopplysninger(
                omsorgsyter = "omsorgsyter",
                omsorgsAr = 2024,
                behandlingOppgaveopplysninger = listOf(
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = UUID.randomUUID(),
                        omsorgsmottaker = "sønn",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Ingen,
                            Oppgaveopplysninger.Ingen,
                        )
                    ),
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = behandlingId,
                        omsorgsmottaker = "datter",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = "omsorgsyter",
                                oppgaveTekst = "tekst for datter"
                            )
                        )
                    )
                ),
                meldingId = meldingId
            )
        ).oppgave

        assertThat(oppgave).isNotNull()
        assertThat(oppgave!!.behandlingId).isEqualTo(behandlingId)
        assertThat(oppgave.meldingId).isEqualTo(meldingId)
        assertThat(oppgave.oppgavetekst).isEqualTo(setOf("tekst for datter"))
    }

    @Test
    fun `filtrerer ut oppgaver for omsorgsmottakere hvor det eksisterer oppgaver fra før`() {
        val behandlingIdSønn = UUID.randomUUID()
        val oppgave = AggregertOppgaveForFullførteBehandlinger(
            omsorgsyterHarOppgave = { s: String, i: Int -> false },
            omsorgsmottakerHarOppgave = { s: String, i: Int -> if (s == "datter") true else false },
            alleOppgaveopplysninger = FullførteBehandlingerOppgaveopplysninger(
                omsorgsyter = "omsorgsyter",
                omsorgsAr = 2024,
                behandlingOppgaveopplysninger = listOf(
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = behandlingIdSønn,
                        omsorgsmottaker = "sønn",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = "omsorgsyter",
                                oppgaveTekst = "tekst for sønn"
                            )
                        )
                    ),
                    FullførtBehandlingOppgaveopplysninger(
                        behandlingId = UUID.randomUUID(),
                        omsorgsmottaker = "datter",
                        oppgaveopplysninger = listOf(
                            Oppgaveopplysninger.Generell(
                                oppgavemottaker = "omsorgsyter",
                                oppgaveTekst = "tekst for datter"
                            )
                        )
                    )
                ),
                meldingId = meldingId
            )
        ).oppgave

        assertThat(oppgave).isNotNull()
        assertThat(oppgave!!.behandlingId).isEqualTo(behandlingIdSønn)
        assertThat(oppgave.meldingId).isEqualTo(meldingId)
        assertThat(oppgave.oppgavetekst).isEqualTo(setOf("tekst for sønn"))
    }
}
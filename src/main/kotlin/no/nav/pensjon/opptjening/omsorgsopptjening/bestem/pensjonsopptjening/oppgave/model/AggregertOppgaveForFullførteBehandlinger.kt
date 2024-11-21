package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
import java.util.UUID


class AggregertOppgaveForFullførteBehandlinger(
    omsorgsyterHarOppgave: (omsorgsyter: String, år: Int) -> Boolean,
    private val omsorgsmottakerHarOppgave: (omsorgsmottaker: String, år: Int) -> Boolean,
    private val alleOppgaveopplysninger: FullførteBehandlingerOppgaveopplysninger,
) {
    val omsorgsyterHar = omsorgsyterHarOppgave(alleOppgaveopplysninger.omsorgsyter, alleOppgaveopplysninger.omsorgsAr)
    val oppgave: Oppgave.Transient? = if (omsorgsyterHar) {
        null
    } else {
        alleOppgaveopplysninger.behandlingOppgaveopplysninger
            .filterNot { omsorgsmottakerHarOppgave(it.omsorgsmottaker, alleOppgaveopplysninger.omsorgsAr) }
            .let { alle ->
                val oppgaverPerBehandling = alle.groupBy { it.behandlingId }
                    .mapValues { it.value.flatMap { it.oppgaveopplysninger.filterIsInstance<Oppgaveopplysninger.Generell>() } }
                    .filter { it.value.isNotEmpty() }

                val alleOppgavetekster = oppgaverPerBehandling.values.flatMap { it.map { it.oppgaveTekst } }.toSet()

                if (alleOppgavetekster.isNotEmpty()) {
                    Oppgave.Transient(
                        detaljer = OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = alleOppgaveopplysninger.omsorgsyter,
                            oppgavetekst = alleOppgavetekster
                        ),
                        //kobler til behandling dersom det bare er oppgaver fra én enkelt behanlding
                        behandlingId = if (oppgaverPerBehandling.keys.count() == 1) oppgaverPerBehandling.keys.first() else null,
                        //kobler til melding uansett, alle behandlinger stammer fra samme melding
                        meldingId = alleOppgaveopplysninger.meldingId
                    )
                } else {
                    null
                }
            }
    }

}

data class FullførteBehandlingerOppgaveopplysninger(
    val omsorgsyter: String,
    val omsorgsAr: Int,
    val behandlingOppgaveopplysninger: List<FullførtBehandlingOppgaveopplysninger>,
    val meldingId: UUID,
)

data class FullførtBehandlingOppgaveopplysninger(
    val behandlingId: UUID,
    val omsorgsmottaker: String,
    val oppgaveopplysninger: List<Oppgaveopplysninger>,
)
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Instant.now
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@Service
class StatusService(
    private val oppgaveRepo: OppgaveRepo,
    private val behandlingRepo: BehandlingRepo,
    private val persongrunnlagRepo: PersongrunnlagRepo,
    private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
) {
    private inline val Int.daysAgo: Instant get() = now().minus(this.days.toJavaDuration())

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun checkStatus(): ApplicationStatus {

        val sisteMelding = persongrunnlagRepo.finnSiste()
        val eldsteUbehandlede : PersongrunnlagMelding? = persongrunnlagRepo.finnEldsteSomIkkeErFerdig()

        if (sisteMelding == null) {
            return ApplicationStatus.IkkeKjort
        } else {
            if (meldingErForGammel(sisteMelding)) {
                return ApplicationStatus.Feil("Siste melding er for gammel")
            }
            if (feiledeMeldinger()) {
                return ApplicationStatus.Feil("Det finnes feilede persongrunnlagmeldinger")
            }
            if (forGammelSomIkkeErFerdig(eldsteUbehandlede)) {
                return ApplicationStatus.Feil("Det finnes gamle meldinger som ikke er ferdig behandlet")
            }
            if (gamleOppgaverSomIkkeErFerdig()) {
                return ApplicationStatus.Feil("Det finnes gamle oppgaver som ikke er ferdig behandlet")
            }
            if (ubehandledGodskrivOpptjening()) {
                return ApplicationStatus.Feil("Det finnes gamle godskrivinger som ikke er ferdig behandlet")
            }
        }
        return ApplicationStatus.OK
    }

    fun meldingErForGammel(persongrunnlagMelding: PersongrunnlagMelding) : Boolean {
        val opprettet: Instant = persongrunnlagMelding.opprettet
        return opprettet < 400.daysAgo
    }

    fun feiledeMeldinger() : Boolean {
        return persongrunnlagRepo.antallMedStatus(PersongrunnlagMelding.Status.Feilet::class) > 0
    }

    fun forGammelSomIkkeErFerdig(persongrunnlagMelding: PersongrunnlagMelding?) : Boolean {
        return persongrunnlagMelding != null && persongrunnlagMelding.opprettet < 2.daysAgo
    }

    fun gamleOppgaverSomIkkeErFerdig() : Boolean {
        var oppgave = oppgaveRepo.finnEldsteUbehandledeOppgave()
        return oppgave != null && oppgave.opprettet < 60.daysAgo
    }

    fun ubehandledGodskrivOpptjening() : Boolean {
        val godskriving = godskrivOpptjeningRepo.finnEldsteIkkeFerdig()
        return godskriving != null && godskriving.opprettet < 7.daysAgo
    }
}

sealed class ApplicationStatus {
    data object OK : ApplicationStatus()
    data object IkkeKjort : ApplicationStatus()
    data class Feil(val feil: String) : ApplicationStatus()
}
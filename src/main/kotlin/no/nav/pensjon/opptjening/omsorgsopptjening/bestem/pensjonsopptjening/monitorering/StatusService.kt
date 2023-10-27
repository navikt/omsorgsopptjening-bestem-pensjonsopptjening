package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

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
    private val persongrunnlagRepo: PersongrunnlagRepo
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun checkStatus(): ApplicationStatus {

        val sisteMelding = persongrunnlagRepo.finnSiste()
        val eldsteUbehandlede : PersongrunnlagMelding? = persongrunnlagRepo.finnEldsteSomIkkeErFerdig()

        if (sisteMelding == null) {
            return ApplicationStatus.Feil("Ingen meldinger")
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
        }
        return ApplicationStatus.Feil("ikke implementert")
    }

    fun meldingErForGammel(persongrunnlagMelding: PersongrunnlagMelding) : Boolean {
        val opprettet: Instant = persongrunnlagMelding.opprettet
        return opprettet < now().minus(400.days.toJavaDuration())
    }

    fun feiledeMeldinger() : Boolean {
        return persongrunnlagRepo.antallMedStatus(PersongrunnlagMelding.Status.Feilet::class) > 0
    }

    fun forGammelSomIkkeErFerdig(persongrunnlagMelding: PersongrunnlagMelding?) : Boolean {
        return persongrunnlagMelding != null
                && persongrunnlagMelding.opprettet < now().minus(2.days.toJavaDuration())
    }

    // behandling:
    fun behandlingEttEllerAnnet() : Boolean {
        // TODO: Finne ut hva man skal sjekke her
        // behandling.meldingId = melding.id
        return false;
    }

    fun oppgaveEttEllerAnnet() : Boolean {
        // TODO: Finne ut hva man skal sjekke her
        // oppgave.meldingId = melding.id
        return false;
    }

    fun ubehandledeOppgaver() : Boolean {
        // oppgave.opprettet < 2 måneder?
        // oppgave.id = oppgave_status.id
        // oppgave_status.kort_status != FERDIG
        return false
    }

    fun ubehandledGodskrivOpptjening() : Boolean {
        // godskriv_opptjening_status.kort_status != FERDIG
        // godskriv_opptjening.opprettet < 1 uke?
        return false;
    }

}



sealed class ApplicationStatus {
    data object OK : ApplicationStatus()
    data object IkkeKjort : ApplicationStatus()
    data class Feil(val feil: String) : ApplicationStatus()
}
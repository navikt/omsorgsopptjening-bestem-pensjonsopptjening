package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant.now
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@Service
class StatusService(private val repo: OppgaveRepo) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun checkStatus(): ApplicationStatus {

        // val sisteInnlesing = repo.finnSisteInnlesing()
        if (null == null) {
            return ApplicationStatus.IkkeKjort
        }
        val minimumTidspunkt = now().minus(400.days.toJavaDuration())
        return ApplicationStatus.Feil("ikke implementert")
    }
}

sealed class ApplicationStatus {
    data object OK : ApplicationStatus()
    data object IkkeKjort : ApplicationStatus()
    class Feil(val feil: String) : ApplicationStatus()
}
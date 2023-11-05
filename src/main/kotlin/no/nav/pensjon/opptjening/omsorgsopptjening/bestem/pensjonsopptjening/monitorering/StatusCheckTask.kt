package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled


class StatusCheckTask(private val statusRapporteringsService: StatusRapporteringCachingAdapter) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @Scheduled(fixedDelay = 120000, initialDelay = 20000)
    fun check() {
        try {
            val status = statusRapporteringsService.oppdaterRapporterbarStatus()
            log.info("Sjekker og oppdaterer status for overvåking: $status")
        } catch(t:RuntimeException) {
            log.error("StatusCheckTask.check() failed",t)
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class StatusRapporteringCachingAdapter (
    private val statusService: StatusService,
    registry: MeterRegistry,
) {
    companion object {
        private lateinit var statusMalere : MicrometerStatusMalere
    }
    init {
        statusMalere = MicrometerStatusMalere(registry)
    }

    fun oppdaterRapporterbarStatus() {
        statusMalere.oppdater(statusService.checkStatus())
    }
}
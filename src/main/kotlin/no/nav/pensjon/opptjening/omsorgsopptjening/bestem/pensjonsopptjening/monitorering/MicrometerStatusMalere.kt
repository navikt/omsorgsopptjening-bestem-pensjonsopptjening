package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.monitorering

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory

class MicrometerStatusMalere(registry: MeterRegistry) {

    private val log = LoggerFactory.getLogger(this::class.java)
    private var status : ApplicationStatus? = null

    init {
        Gauge
            .builder("pensjonopptjening_applikasjonsstatus_ok") { antallOk() }
            .tag("status","ok")
            .register(registry)
        Gauge
            .builder("pensjonopptjening_applikasjonsstatus_feil") { antallFeil() }
            .tag("status","feil")
            .tag("feilmelding",feilmelding())
            .register(registry)
        Gauge
            .builder("pensjonopptjening_applikasjonsstatus_ukjent") { antallMangler() }
            .tag("status","ukjent")
            .register(registry)
        // TODO: skrive om til å bli penere + test
        Gauge
            .builder("pensjonopptjening_applikasjonsstatus_sum") { antallOk() + antallFeil() + antallMangler() }
            .tag("status","ukjent")
            .register(registry)
        Gauge
            .builder("pensjonopptjening_applikasjonsstatus_kode") { statusKode() }
            .tag("status", status?.toString()?:"null")
            .register(registry)
    }

    fun oppdater(status: ApplicationStatus) {
        log.info("Oppdaterer applikasjonsstatus til: $status")
        this.status = status
        status
    }

    fun antallOk() : Int {
        return if (status == ApplicationStatus.OK) 9 else 0
    }

    fun antallFeil() : Int {
        return if (status is ApplicationStatus.Feil) 10 else 0
    }

    fun statusKode() : Int {
        return when (status) {
            null -> 0
            is ApplicationStatus.OK -> 1
            is ApplicationStatus.IkkeKjort -> 2
            is ApplicationStatus.Feil -> 3
        }
    }

    fun antallMangler() : Int {
        if (status == null) log.warn("applikasjonsstatus har ikke blitt satt")
        val mangler = (status == null || status is ApplicationStatus.IkkeKjort)
        return if (mangler) 1 else 0
    }

    fun feilmelding() : String {
        val status = this.status
        return if (status is ApplicationStatus.Feil) status.feil else ""
    }

}
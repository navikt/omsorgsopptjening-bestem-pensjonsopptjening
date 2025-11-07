package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.TimeLock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KontrollbehandlingProcessingTask(
    private val service: KontrollbehandlingProcessingService,
    private val unleash: UnleashWrapper,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
    private val timeLockProperties: TimeLock.Properties,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun run() {
        val timeLock = TimeLock(
            properties = timeLockProperties
        )
        while (true) {
            if (timeLock.isOpen()) {
                try {
                    if (unleash.isEnabled(NavUnleashConfig.Feature.KONTROLL) && datasourceReadinessCheck.isReady()) {
                        when (service.process()) {
                            is Resultat.FantIngenDataÅProsessere -> {
                                timeLock.lock()
                            }

                            is Resultat.Prosessert -> {
                                timeLock.open()
                            }
                        }
                    }
                } catch (exception: Throwable) {
                    log.warn("Exception caught while processing ${exception::class.qualifiedName}")
                    secureLog.error("Exception caught while processing", exception)
                    timeLock.lock()
                }
            } else {
                timeLock.lockDuration().let {
                    Thread.sleep(it)
                }
            }
        }
    }
}
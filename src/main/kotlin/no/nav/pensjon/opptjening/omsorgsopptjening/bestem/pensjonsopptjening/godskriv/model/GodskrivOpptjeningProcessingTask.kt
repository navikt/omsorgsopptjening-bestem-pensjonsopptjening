package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.TimeLock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.metrics.GodskrivProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GodskrivOpptjeningProcessingTask(
    private val service: GodskrivOpptjeningProcessingService,
    private val unleash: UnleashWrapper,
    private val godskrivProcessingMetricsMåling: GodskrivProcessingMetrikker,
    private val godskrivProcessingMetricsFeilmåling: GodskrivProcessingMetricsFeilmåling,
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
                    if (unleash.isEnabled(NavUnleashConfig.Feature.GODSKRIV) && datasourceReadinessCheck.isReady()) {
                        godskrivProcessingMetricsMåling.oppdater {
                            when (val result = service.process()) {
                                is Resultat.FantIngenDataÅProsessere -> {
                                    timeLock.lock()
                                    emptyList()
                                }

                                is Resultat.Prosessert -> {
                                    timeLock.open()
                                    result.data
                                }
                            }
                        }
                    }
                } catch (exception: Throwable) {
                    godskrivProcessingMetricsFeilmåling.oppdater {
                        log.warn("Exception caught while processing ${exception::class.qualifiedName}")
                        secureLog.error("Exception caught while processing", exception)
                    }
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
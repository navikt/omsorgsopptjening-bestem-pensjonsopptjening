package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.metrics.BrevProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.TimeLock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration

class BrevProcessingTask(
    private val service: BrevService,
    private val unleash: UnleashWrapper,
    private val brevProcessingMetricsMåling: BrevProcessingMetrikker,
    private val brevProcessingMetricsFeilmåling: BrevProcessingMetricsFeilmåling,
    private val datasourceReadinessCheck: DatasourceReadinessCheck,
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun run() {
        val timeLock = TimeLock(
            initialDelay = Duration.ofMinutes(1),
            maxDelay = Duration.ofMinutes(2),
            clock = Clock.systemUTC()
        )
        while (true) {
            if (timeLock.isOpen()) {
                try {
                    if (unleash.isEnabled(NavUnleashConfig.Feature.BREV) && datasourceReadinessCheck.isReady()) {
                        brevProcessingMetricsMåling.oppdater {
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
                    brevProcessingMetricsFeilmåling.oppdater {
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
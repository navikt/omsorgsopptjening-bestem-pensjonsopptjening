package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.DatasourceReadinessCheck
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.config.TimeLock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetricsFeilmåling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.metrics.OppgaveProcessingMetrikker
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.NavUnleashConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.unleash.UnleashWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class OppgaveProcessingTask(
    private val service: OppgaveService,
    private val unleash: UnleashWrapper,
    private val oppgaveProcessingMetricsMåling: OppgaveProcessingMetrikker,
    private val oppgaveProcessingMetricsFeilmåling: OppgaveProcessingMetricsFeilmåling,
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
                    if (unleash.isEnabled(NavUnleashConfig.Feature.OPPRETT_OPPGAVER) && datasourceReadinessCheck.isReady()) {
                        oppgaveProcessingMetricsMåling.oppdater {
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
                    oppgaveProcessingMetricsFeilmåling.oppdater {
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
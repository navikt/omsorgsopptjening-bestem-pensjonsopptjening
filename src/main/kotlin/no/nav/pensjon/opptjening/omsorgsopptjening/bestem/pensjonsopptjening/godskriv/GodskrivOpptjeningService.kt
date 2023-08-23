package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class GodskrivOpptjeningService(
    private val client: PoppClient,
    private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
    private val behandlingRepo: BehandlingRepo,
) {
    @Autowired
    private lateinit var statusoppdatering: Statusoppdatering

    /**
     * https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
     *
     * "In proxy mode (which is the default), only external method calls coming in through the proxy are intercepted.
     * This means that self-invocation (in effect, a method within the target object calling another method of the target object)
     * does not lead to an actual transaction at runtime even if the invoked method is marked with @Transactional.
     * Also, the proxy must be fully initialized to provide the expected behavior, so you should not rely on this feature
     * in your initialization code - for example, in a @PostConstruct method."
     */
    @Component
    private class Statusoppdatering(
        private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
        private val oppgaveService: OppgaveService,
    ) {
        @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRES_NEW)
        fun markerForRetry(
            godskrivOpptjening: GodskrivOpptjening,
            exception: Throwable
        ) {
            godskrivOpptjening.retry(exception.toString()).let { retry ->
                retry.opprettOppgave()?.let {
                    log.error("Gir opp videre prosessering av godskriv opptjening")
                    oppgaveService.opprett(it)
                }
                godskrivOpptjeningRepo.updateStatus(retry)
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun opprett(godskrivOpptjening: GodskrivOpptjening): GodskrivOpptjening {
        return godskrivOpptjeningRepo.persist(godskrivOpptjening)
    }

    @Transactional(rollbackFor = [Throwable::class])
    fun process(): GodskrivOpptjening? {
        return godskrivOpptjeningRepo.finnNesteUprosesserte()?.let { godskrivOpptjening ->
            //TODO kan vurdere å hente behandlingen med godskriv oppgjening objektet
            behandlingRepo.finn(godskrivOpptjening.behandlingId).let { behandling ->
                Mdc.scopedMdc(CorrelationId.name, godskrivOpptjening.correlationId.toString()) {
                    try {
                        log.info("Lagrer i popp")
                        client.lagre(
                            omsorgsyter = behandling.omsorgsyter,
                            omsorgsÅr = behandling.omsorgsAr,
                            omsorgstype = behandling.omsorgstype,
                            kilde = behandling.kilde(),
                            omsorgsmottaker = behandling.omsorgsmottaker,
                        )
                        godskrivOpptjening.ferdig().also {
                            godskrivOpptjeningRepo.updateStatus(it)
                            log.info("Lagret i popp")
                        }
                    } catch (exception: Throwable) {
                        log.warn("Exception caught while processing item: ${godskrivOpptjening.id}, exeption:$exception")
                        statusoppdatering.markerForRetry(godskrivOpptjening, exception)
                        throw exception
                    }
                }
            }
        }
    }
}
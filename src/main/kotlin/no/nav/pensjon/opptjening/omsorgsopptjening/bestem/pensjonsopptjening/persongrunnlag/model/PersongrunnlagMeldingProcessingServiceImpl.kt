package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class PersongrunnlagMeldingProcessingServiceImpl(
    private val transactionTemplate: NewTransactionTemplate,
    private val service: PersongrunnlagMeldingService,
) : PersongrunnlagMeldingProcessingService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun process(): List<FullførteBehandlinger>? {
        val meldinger = transactionTemplate.execute { service.hentOgLås(10) }!!
        try {
            return meldinger.data.mapNotNull { melding ->
                Mdc.scopedMdc(melding.correlationId) {
                    Mdc.scopedMdc(melding.innlesingId) {
                        try {
                            log.info("Started behandling av melding")
                            transactionTemplate.execute {
                                service.behandle(melding).also { log.info("Melding prosessert") }
                            }
                        } catch (ex: Throwable) {
                            // TODO: SQLException og andre tekniske feil bør ikke medføre retry, kun rollback
                            transactionTemplate.execute {
                                log.warn("Exception ved prosessering av melding: ${ex::class.qualifiedName}")
                                secureLog.warn("Exception ved prosessering av melding", ex)
                                service.retry(melding, ex)
                            }
                            null
                        } finally {
                            log.info("Avsluttet behandling av melding")
                        }
                    }
                }
            }
        } catch (ex: Throwable) {
            log.error("Fikk exception ved uthenting av meldinger: ${ex::class.qualifiedName}")
            secureLog.error("Exception ved uthenting av meldinger: ", ex)
            return null // throw ex
        } finally {
            transactionTemplate.execute { service.frigi(meldinger) }
        }
    }
}
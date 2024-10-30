package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

internal class PersongrunnlagMeldingProcessingServiceImpl(
    private val transactionTemplate: NewTransactionTemplate,
    private val service: PersongrunnlagMeldingService,
) : PersongrunnlagMeldingProcessingService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun process(): Resultat<List<FullførteBehandlinger>> {
        val meldinger = transactionTemplate.execute { service.hentOgLås(10) }!!
            .also { it.data.ifEmpty { return Resultat.FantIngenDataÅProsessere() } }

        return try {
            Resultat.Prosessert(
                meldinger.data.mapNotNull { melding ->
                    Mdc.scopedMdc(melding.correlationId) {
                        Mdc.scopedMdc(melding.innlesingId) {
                            try {
                                log.info("Started behandling av melding")
                                transactionTemplate.execute {
                                    service.behandle(melding).also { log.info("Melding prosessert") }
                                }
                            } catch (ex: SQLException) {
                                throw ex
                            } catch (ex: Throwable) {
                                log.warn("Exception ved prosessering av melding: ${ex::class.qualifiedName}")
                                secureLog.warn("Exception ved prosessering av melding", ex)
                                transactionTemplate.execute {
                                    service.retry(melding, ex)
                                }
                                null
                            } finally {
                                log.info("Avsluttet behandling av melding")
                            }
                        }
                    }
                }
            )
        } finally {
            transactionTemplate.execute { service.frigi(meldinger) }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

internal class KontrollbehandlingProcessingServiceImpl(
    private val transactionTemplate: NewTransactionTemplate,
    private val service: KontrollbehandlingService,
) : KontrollbehandlingProcessingService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun process(): Resultat<List<FullførteBehandlinger>> {
        val meldinger = transactionTemplate.execute { service.hentOgLås(10) }!!
            .also { it.rader.ifEmpty { return Resultat.FantIngenDataÅProsessere() } }

        return try {
            Resultat.Prosessert(
                meldinger.rader.mapNotNull { melding ->
                    Mdc.scopedMdc(CorrelationId(melding.kontrollId)) {
                        Mdc.scopedMdc(InnlesingId(melding.kontrollId)) {
                            try {
                                log.info("Starter kontrollbehandling")
                                transactionTemplate.execute {
                                    service.behandle(melding).also { log.info("Kontrollbehandling utført") }
                                }
                            } catch (ex: SQLException) {
                                throw ex
                            } catch (ex: Throwable) {
                                log.warn("Exception ved prosessering av kontrollbehandling: ${ex::class.qualifiedName}")
                                secureLog.warn("Exception ved prosessering av kontrollbehandling", ex)
                                transactionTemplate.execute {
                                    service.retry(melding, ex)
                                }
                                null
                            } finally {
                                log.info("Avslutter kontrollbehandling")
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

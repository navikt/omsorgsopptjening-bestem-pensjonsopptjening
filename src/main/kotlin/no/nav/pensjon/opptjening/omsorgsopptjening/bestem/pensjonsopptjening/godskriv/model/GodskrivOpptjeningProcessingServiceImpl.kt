package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

class GodskrivOpptjeningProcessingServiceImpl(
    private val godskrivOpptjeningService: GodskrivOpptjeningService,
    private val transactionTemplate: NewTransactionTemplate,
) : GodskrivOpptjeningProcessingService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun process(): Resultat<List<GodskrivOpptjening.Persistent>> {
        val låsteGodskrivOpptjeninger = transactionTemplate.execute { godskrivOpptjeningService.hentOgLås(10) }!!
            .also { it.data.ifEmpty { return Resultat.FantIngenDataÅProsessere() } }

        return try {
            Resultat.Prosessert(
                låsteGodskrivOpptjeninger.data.mapNotNull { godskrivOpptjening ->
                    Mdc.scopedMdc(godskrivOpptjening.correlationId) {
                        Mdc.scopedMdc(godskrivOpptjening.innlesingId) {
                            try {
                                transactionTemplate.execute {
                                    godskrivOpptjeningService.håndter(godskrivOpptjening)
                                }
                            } catch (ex: SQLException) {
                                throw ex
                            } catch (ex: Throwable) {
                                log.warn("Exception ved prosessering av godskriving: ${ex::class.qualifiedName}")
                                secureLog.warn("Exception ved prosessering av godskriving", ex)
                                transactionTemplate.execute {
                                    godskrivOpptjeningService.retry(godskrivOpptjening, ex)
                                }
                                null
                            }
                        }
                    }
                }
            )
        } finally {
            godskrivOpptjeningService.frigiLås(låsteGodskrivOpptjeninger)
        }
    }
}

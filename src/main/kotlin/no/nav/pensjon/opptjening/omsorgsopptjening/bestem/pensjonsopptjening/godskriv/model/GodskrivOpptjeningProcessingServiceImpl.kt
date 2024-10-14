package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

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
    }

    override fun process(): List<GodskrivOpptjening.Persistent>? {
        val låsteGodskrivOpptjeninger = transactionTemplate.execute { godskrivOpptjeningService.hentOgLås(10) }!!

        return try {
            låsteGodskrivOpptjeninger.data.mapNotNull { godskrivOpptjening ->
                Mdc.scopedMdc(godskrivOpptjening.correlationId) {
                    Mdc.scopedMdc(godskrivOpptjening.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                godskrivOpptjeningService.håndter(godskrivOpptjening)
                            }
                        } catch (ex: SQLException) {
                            log.error("Feil under prosessering av godskriv opptjening", ex)
                            throw ex
                        } catch (ex: Throwable) {
                            try {
                                transactionTemplate.execute {
                                    godskrivOpptjeningService.retry(godskrivOpptjening, ex)
                                }
                            } catch (ex: Throwable) {
                                log.error("Feil ved setting av feilstatus: ${ex::class.qualifiedName}")
                            }
                            null
                        }
                    }
                }
            }
        } finally {
            godskrivOpptjeningService.frigiLås(låsteGodskrivOpptjeninger)
        }
    }
}

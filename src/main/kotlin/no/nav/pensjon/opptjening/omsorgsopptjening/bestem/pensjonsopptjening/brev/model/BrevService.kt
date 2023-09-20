package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Component
class BrevService(
    private val brevRepository: BrevRepository,
    private val transactionTemplate: TransactionTemplate
) {
    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprett(brev: Brev): Brev {
        return brevRepository.persist(brev)
    }

    fun process() {
        transactionTemplate.execute {
            brevRepository.finnNesteUprosesserte()?.let { brev ->
                Mdc.scopedMdc(brev.correlationId) {
                    Mdc.scopedMdc(brev.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                brev.ferdig().also {
                                    brevRepository.updateStatus(it)
                                    //TODO gjør et eller annet
                                }
                            }
                        } catch (ex: Throwable) {
                            transactionTemplate.execute {
                                brev.retry(ex.toString()).also {
                                    brevRepository.updateStatus(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
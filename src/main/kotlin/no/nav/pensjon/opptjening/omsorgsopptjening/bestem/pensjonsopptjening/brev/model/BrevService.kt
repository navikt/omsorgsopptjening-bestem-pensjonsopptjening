package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Component
class BrevService(
    private val brevRepository: BrevRepository,
    private val transactionTemplate: TransactionTemplate,
    private val brevClient: BrevClient,
    private val bestemSak: BestemSakKlient,
    private val personOppslag: PersonOppslag,
) {
    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprett(brev: Brev): Brev {
        return brevRepository.persist(brev)
    }

    fun process(): Brev? {
        return transactionTemplate.execute {
            brevRepository.finnNesteUprosesserte()?.let { brev ->
                Mdc.scopedMdc(brev.correlationId) {
                    Mdc.scopedMdc(brev.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                personOppslag.hentAktørId(brev.omsorgsyter).let { aktørId ->
                                    bestemSak.bestemSak(aktørId).let { sak ->
                                        brevClient.sendBrev(
                                            sakId = sak.sakId,
                                            fnr = brev.omsorgsyter,
                                            omsorgsår = brev.omsorgsår,
                                        ).let { journalpost ->
                                            brev.ferdig(journalpost).also {
                                                brevRepository.updateStatus(it)
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ex: Throwable) {
                            transactionTemplate.execute {
                                brev.retry(ex.toString()).also {
                                    brevRepository.updateStatus(it)
                                }
                            }
                            throw ex
                        }
                    }
                }
            }
        }
    }
}
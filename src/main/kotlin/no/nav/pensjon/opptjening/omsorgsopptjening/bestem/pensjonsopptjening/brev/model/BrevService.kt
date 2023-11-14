package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Brevopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Foreldre
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.Pensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.Year

@Component
class BrevService(
    private val brevRepository: BrevRepository,
    private val transactionTemplate: TransactionTemplate,
    private val brevClient: BrevClient,
    private val bestemSak: BestemSakKlient,
    private val personOppslag: PersonOppslag,
    private val hentPensjonspoeng: HentPensjonspoengClient,
) {
    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprett(brev: Brev.Transient): Brev.Persistent {
        return brevRepository.persist(brev)
    }

    @Transactional(rollbackFor = [Throwable::class], propagation = Propagation.REQUIRED)
    fun opprettHvisNødvendig(behandling: FullførtBehandling) {
        val brevopplysninger = behandling.hentBrevopplysninger(
            hentPensjonspoengForOmsorgsopptjening = hentPensjonspoeng::hentPensjonspoengForOmsorgstype,
            hentPensjonspoengForInntekt = hentPensjonspoeng::hentPensjonspoengForInntekt
        )

        when (brevopplysninger) {
            is Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker -> {
                opprett(Brev.Transient(behandling.id, brevopplysninger.årsak))
            }

            Brevopplysninger.Ingen -> {
                //noop
            }
        }
    }

    fun process(): Brev.Persistent? {
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
                                            omsorgsår = Year.of(brev.omsorgsår),
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
                                brev.retry(ex.stackTraceToString()).also {
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
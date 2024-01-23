package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Brevopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakKlient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.time.Year
import java.util.*

@Component
class BrevService(
    private val brevRepository: BrevRepository,
    private val transactionTemplate: TransactionTemplate,
    private val brevClient: BrevClient,
    private val bestemSak: BestemSakKlient,
    private val personOppslag: PersonOppslag,
    private val hentPensjonspoeng: HentPensjonspoengClient,
) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)!!
    }

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

    fun process(): List<Brev.Persistent>? {
        val låsteBrev = brevRepository.finnNesteUprosesserte(10)
        try {
            return låsteBrev.data.mapNotNull { brev ->
                Mdc.scopedMdc(brev.correlationId) {
                    Mdc.scopedMdc(brev.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                personOppslag.hentAktørId(brev.omsorgsyter).let { aktørId ->
                                    bestemSak.bestemSak(aktørId).let { sak ->
                                        brevClient.sendBrev(
                                            sakId = sak.sakId,
                                            eksternReferanseId = EksternReferanseId(brev.id.toString()),
                                            omsorgsår = Year.of(brev.omsorgsår),
                                        ).let { journalpost ->
                                            brev.ferdig(journalpost).also {
                                                brevRepository.updateStatus(it)
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ex: SQLException) {
                            log.error("Feil ved prosessering av brev", ex)
                            throw ex
                        } catch (ex: Throwable) {
                            try {
                                transactionTemplate.execute {
                                    brev.retry(ex.stackTraceToString()).also {
                                        brevRepository.updateStatus(it)
                                    }
                                }
                            } catch (ex: Throwable) {
                                log.error("Feil ved setting av feil-status på brev: ${ex::class.qualifiedName}")
                                null
                            }
                        }
                    }
                }
            }
        } finally {
            brevRepository.frigi(låsteBrev)
        }
    }

    fun stoppForMelding(meldingsId: UUID) {
        brevRepository.findForMelding(meldingsId).forEach { brev ->
            when (brev.status) {
                is Brev.Status.Ferdig -> Unit
                is Brev.Status.Stoppet -> Unit
                else -> {
                    log.info("Stopper brev: ${brev.id}")
                    brev.stoppet().let {
                        brevRepository.updateStatus(it)
                    }
                }
            }
        }
    }

    fun stopp(id: UUID): UUID? {
        return brevRepository.tryFind(id)?.let { brev ->
            when (brev.status) {
                is Brev.Status.Ferdig -> brev.id
                is Brev.Status.Stoppet -> brev.id
                else -> {
                    log.info("Stopper brev: ${brev.id}")
                    brev.stoppet().let {
                        brevRepository.updateStatus(it)
                        it.id
                    }
                }
            }
        }
    }

    fun restart(brevId: UUID): UUID? {
        return transactionTemplate.execute {
            brevRepository.find(brevId).let {
                it.restart()
            }.let {
                brevRepository.updateStatus(it)
                brevId
            }
        }
    }
}
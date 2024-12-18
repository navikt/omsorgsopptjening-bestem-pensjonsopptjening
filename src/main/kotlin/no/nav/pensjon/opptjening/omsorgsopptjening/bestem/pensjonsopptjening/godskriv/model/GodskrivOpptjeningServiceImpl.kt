package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

internal class GodskrivOpptjeningServiceImpl(
    private val godskrivClient: GodskrivOpptjeningClient,
    private val godskrivOpptjeningRepo: GodskrivOpptjeningRepo,
    private val behandlingRepo: BehandlingRepo,
    private val oppgaveService: OppgaveService,
) : GodskrivOpptjeningService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun opprett(godskrivOpptjening: GodskrivOpptjening.Transient): GodskrivOpptjening.Persistent {
        return godskrivOpptjeningRepo.persist(godskrivOpptjening)
    }


    override fun håndter(godskrivOpptjening: GodskrivOpptjening.Persistent): GodskrivOpptjening.Persistent {
        return behandlingRepo.finn(godskrivOpptjening.behandlingId).let { behandling ->
            godskrivOpptjening.ferdig().also {
                godskrivClient.godskriv(
                    omsorgsyter = behandling.omsorgsyter,
                    omsorgsÅr = behandling.omsorgsAr,
                    omsorgstype = behandling.omsorgstype,
                    omsorgsmottaker = behandling.omsorgsmottaker,
                )
                godskrivOpptjeningRepo.updateStatus(it)
            }
        }
    }

    override fun retry(godskrivOpptjening: GodskrivOpptjening.Persistent, ex: Throwable) {
        return godskrivOpptjening.retry(ex.stackTraceToString()).let { retry ->
            retry.opprettOppgave()?.let {
                log.error("Gir opp videre prosessering av godskriv opptjening")
                oppgaveService.opprett(it)
            }
            godskrivOpptjeningRepo.updateStatus(retry)
        }
    }


    override fun stoppForMelding(meldingsId: UUID, begrunnelse: String?) {
        godskrivOpptjeningRepo.findForMelding(meldingsId).forEach { godkjenn ->
            log.info("Stopper godkjenning: ${godkjenn.id}")
            godkjenn.stoppet(begrunnelse).let {
                godskrivOpptjeningRepo.updateStatus(it)
            }
        }
    }

    override fun stopp(id: UUID, begrunnelse: String?): UUID? {
        log.info("Stopper godkjenning: $id")
        return godskrivOpptjeningRepo.tryFind(id)?.stoppet(begrunnelse)?.let {
            godskrivOpptjeningRepo.updateStatus(it)
            id
        }
    }

    override fun restart(id: UUID): UUID? {
        log.info("Stopper godkjenning: $id")
        return godskrivOpptjeningRepo.tryFind(id)?.klar()?.let {
            godskrivOpptjeningRepo.updateStatus(it)
            id
        }
    }

    override fun hentOgLås(antall: Int): GodskrivOpptjeningRepo.Locked {
        return godskrivOpptjeningRepo.finnNesteUprosesserte(10)
    }

    override fun frigiLås(låst: GodskrivOpptjeningRepo.Locked) {
        return godskrivOpptjeningRepo.frigi(låst)
    }
}
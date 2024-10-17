package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.time
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VurderVilkår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.OmsorgsopptjeningsgrunnlagService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KontrollbehandlingServiceImpl(
    private val omsorgsopptjeningsgrunnlagService: OmsorgsopptjeningsgrunnlagService,
    private val kontrollbehandlingRepo: KontrollbehandlingRepo,
    private val behandlingRepo: BehandlingRepo,
    private val persongrunnlagRepo: PersongrunnlagRepo,
) : KontrollbehandlingService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun behandle(kontrollrad: Kontrollbehandling): FullførteBehandlinger {
        val behandling = behandlingRepo.finnUtenJson(kontrollrad.orginalBehandlingId)
        val persongrunnlag = persongrunnlagRepo.find(behandling.meldingId)
        val nyttGrunnlag = omsorgsopptjeningsgrunnlagService.lagOmsorgsopptjeningsgrunnlag(persongrunnlag)
            .singleOrNull { it.omsorgsyter.fnr == behandling.omsorgsyter && it.omsorgsAr == behandling.omsorgsAr && it.omsorgsmottaker.fnr == behandling.omsorgsmottaker }
            ?: throw IllegalStateException("Skal eksistere")

        val vurderVilkår: VurderVilkår = VilkårsvurderingFactory(
            grunnlag = nyttGrunnlag,
            finnForOmsorgsyterOgÅr = {
                time("kontrollbehandlingRepo.finnForOmsorgsyterOgAr") {
                    kontrollbehandlingRepo.finnForOmsorgsyterOgAr(
                        nyttGrunnlag.omsorgsyter.fnr,
                        nyttGrunnlag.omsorgsAr,
                        kontrollrad.referanse,
                    )
                }
            },
            finnForOmsorgsmottakerOgÅr = {
                time("kontrollbehandlingRepo.finnForOmsorgsmottakerOgAr") {
                    kontrollbehandlingRepo.finnForOmsorgsmottakerOgAr(
                        nyttGrunnlag.omsorgsmottaker.fnr,
                        nyttGrunnlag.omsorgsAr,
                        kontrollrad.referanse,
                    )
                }
            },
        )

        val nyBehandling = Behandling(
            grunnlag = nyttGrunnlag,
            vurderVilkår = vurderVilkår,
            meldingId = behandling.meldingId
        )

        return FullførteBehandlinger(
            listOf(
                kontrollbehandlingRepo.update(
                    kontrollrad,
                    nyBehandling
                )
            )
        ).also { kontrollbehandlingRepo.updateStatus(kontrollrad.ferdig()) }
    }

    override fun retry(kontrollrad: Kontrollbehandling, ex: Throwable) {
        kontrollrad.retry(ex.stackTraceToString()).let { kontrollbehandlingRepo.updateStatus(it) }
    }

    override fun hentOgLås(antall: Int): KontrollbehandlingRepo.Locked {
        return kontrollbehandlingRepo.finnNesteMeldingerForBehandling(antall)
    }

    override fun frigi(locked: KontrollbehandlingRepo.Locked) {
        return kontrollbehandlingRepo.frigi(locked)
    }

    override fun kontrollbehandling(innlesingId: InnlesingId, referanse: String) {
        return kontrollbehandlingRepo.bestillKontroll(innlesingId, referanse)
    }
}
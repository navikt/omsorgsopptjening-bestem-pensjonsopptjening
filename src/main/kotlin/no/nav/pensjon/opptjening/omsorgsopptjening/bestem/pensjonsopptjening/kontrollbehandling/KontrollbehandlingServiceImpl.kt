package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.metrics.time
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Brevopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.HentPensjonspoengClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.OmsorgsopptjeningsgrunnlagService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KontrollbehandlingServiceImpl(
    private val omsorgsopptjeningsgrunnlagService: OmsorgsopptjeningsgrunnlagService,
    private val kontrollbehandlingRepo: KontrollbehandlingRepo,
    private val persongrunnlagRepo: PersongrunnlagRepo,
    private val hentPensjonspoeng: HentPensjonspoengClient,
) : KontrollbehandlingService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun behandle(kontrollrad: Kontrollbehandling): FullførteBehandlinger {
        val melding = persongrunnlagRepo.find(kontrollrad.kafkameldingid)
        return FullførteBehandlinger(
            behandlinger = omsorgsopptjeningsgrunnlagService.lagOmsorgsopptjeningsgrunnlag(melding)
                .filter { it.omsorgsAr == kontrollrad.omsorgsÅr }
                .map {
                    kontrollbehandlingRepo.persist(
                        kontrollrad,
                        Behandling(
                            grunnlag = it,
                            vurderVilkår = VilkårsvurderingFactory(
                                grunnlag = it,
                                finnForOmsorgsyterOgÅr = {
                                    time("kontrollbehandlingRepo.finnForOmsorgsyterOgAr") {
                                        kontrollbehandlingRepo.finnForOmsorgsyterOgAr(
                                            it.omsorgsyter.fnr,
                                            it.omsorgsAr,
                                            kontrollrad.referanse,
                                        )
                                    }
                                },
                                finnForOmsorgsmottakerOgÅr = {
                                    time("kontrollbehandlingRepo.finnForOmsorgsmottakerOgAr") {
                                        kontrollbehandlingRepo.finnForOmsorgsmottakerOgAr(
                                            it.omsorgsmottaker.fnr,
                                            it.omsorgsAr,
                                            kontrollrad.referanse,
                                        )
                                    }
                                },
                            ),
                            meldingId = kontrollrad.kontrollmeldingId
                        )
                    )
                }
        ).also { fullførteBehandlinger ->
            fullførteBehandlinger.håndterUtfall(
                innvilget = {
                    kontrollbehandlingRepo.oppdaterMedGodskriv(it, it.godskrivOpptjening())
                    it.hentBrevopplysninger(
                        hentPensjonspoengForOmsorgsopptjening = hentPensjonspoeng::hentPensjonspoengForOmsorgstype,
                        hentPensjonspoengForInntekt = hentPensjonspoeng::hentPensjonspoengForInntekt
                    ).let { brevopplysninger ->
                        when (brevopplysninger) {
                            is Brevopplysninger.InfobrevOmsorgsyterForHjelpestønadsmottaker -> {
                                kontrollbehandlingRepo.oppdaterMedBrev(
                                    fullførtBehandling = it,
                                    brevopplysninger = brevopplysninger
                                )
                            }

                            Brevopplysninger.Ingen -> {} //noop
                        }
                    }
                },
                manuell = {
                    if (it.hentOppgaveopplysninger().isNotEmpty()) {
                        //TODO litt mikk for å få dette helt likt som oppgaveservice
                        kontrollbehandlingRepo.oppdaterMedOppgave(it, it.hentOppgaveopplysninger())
                    }
                },
                avslag = {} //noop,
            )
            kontrollbehandlingRepo.updateStatus(kontrollrad.ferdig())
        }
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

    override fun kontrollbehandling(innlesingId: InnlesingId, referanse: String, år: Int) {
        return kontrollbehandlingRepo.bestillKontroll(innlesingId, referanse, år)
    }
}
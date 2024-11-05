package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kontrollbehandling

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Brevopplysninger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Oppgaveopplysninger
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

    override fun behandle(kontrollbehandling: Kontrollbehandling): FullførteBehandlinger {
        val melding = persongrunnlagRepo.find(kontrollbehandling.kafkameldingid)
        return FullførteBehandlinger(
            behandlinger = omsorgsopptjeningsgrunnlagService.lagOmsorgsopptjeningsgrunnlag(melding)
                .filter { it.omsorgsAr == kontrollbehandling.omsorgsÅr }
                .map {
                    kontrollbehandlingRepo.persist(
                        kontrollbehandling,
                        Behandling(
                            grunnlag = it,
                            vurderVilkår = VilkårsvurderingFactory(
                                grunnlag = it,
                                finnForOmsorgsyterOgÅr = {
                                    kontrollbehandlingRepo.finnForOmsorgsyterOgAr(
                                        fnr = it.omsorgsyter.fnr,
                                        ar = it.omsorgsAr,
                                        referanse = kontrollbehandling.referanse,
                                    )
                                },
                                finnForOmsorgsmottakerOgÅr = {
                                    kontrollbehandlingRepo.finnForOmsorgsmottakerOgAr(
                                        omsorgsmottaker = it.omsorgsmottaker.fnr,
                                        ar = it.omsorgsAr,
                                        referanse = kontrollbehandling.referanse,
                                    )
                                },
                                finnForOmsorgsytersAndreBarnOgÅr = {
                                    kontrollbehandlingRepo.finnForOmsorgsytersAndreBarn(
                                        omsorgsyter = it.omsorgsyter.fnr,
                                        ar = it.omsorgsAr,
                                        andreBarnEnnOmsorgsmottaker = it.omsorgsyter.finnAndreBarnEnn(it.omsorgsmottaker.fnr)
                                            .map { it.ident },
                                        referanse = kontrollbehandling.referanse,
                                    )
                                }
                            ),
                            meldingId = kontrollbehandling.kontrollmeldingId
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
                manuell = { b ->
                    //TODO litt mikk for å få dette helt likt som oppgaveservice
                    b.hentOppgaveopplysninger()
                        .filterNot { it is Oppgaveopplysninger.Ingen }
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            kontrollbehandlingRepo.oppdaterMedOppgave(b, it)
                        }
                },
                avslag = {} //noop,
            )
            kontrollbehandlingRepo.updateStatus(kontrollbehandling.ferdig())
        }
    }

    override fun retry(kontrollbehandling: Kontrollbehandling, ex: Throwable) {
        kontrollbehandling.retry(ex.stackTraceToString()).let { kontrollbehandlingRepo.updateStatus(it) }
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
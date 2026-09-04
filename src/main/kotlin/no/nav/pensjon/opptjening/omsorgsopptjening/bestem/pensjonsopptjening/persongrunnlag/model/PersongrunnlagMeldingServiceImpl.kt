package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AggregertBehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erUbestemt
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID


internal class PersongrunnlagMeldingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val persongrunnlagRepo: PersongrunnlagRepo,
    private val oppgaveService: OppgaveService,
    private val godskrivOpptjeningService: GodskrivOpptjeningService,
    private val transactionTemplate: NewTransactionTemplate,
    private val brevService: BrevService,
    private val omsorgsopptjeningsgrunnlagService: OmsorgsopptjeningsgrunnlagService,
) : PersongrunnlagMeldingService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    override fun behandle(melding: PersongrunnlagMelding.Mottatt): FullførteBehandlinger {
        return if (melding.harFeilinformasjon()) {
            behandleFeilinformasjon(melding)
            FullførteBehandlinger(emptyList())
        } else {
            behandleIntern(melding).let { fullførte ->
                persongrunnlagRepo.updateStatus(melding.ferdig())
                fullførte.also {
                    it.håndterUtfall(
                        innvilget = ::håndterInnvilgelse,
                        manuell = oppgaveService::opprettOppgaveHvisNødvendig,
                        avslag = {} //noop
                    )
                }
            }
        }
    }

    private fun behandleFeilinformasjon(melding: PersongrunnlagMelding.Mottatt): Oppgave.Persistent {
        return oppgaveService.opprett(melding.opprettOppgave()!!).also {
            persongrunnlagRepo.updateStatus(melding.ferdig())
        }
    }

    override fun retry(melding: PersongrunnlagMelding.Mottatt, ex: Throwable) {
        melding.retry(ex.stackTraceToString()).let { retry ->
            retry.opprettOppgave()?.let { oppgave ->
                log.error("Gir opp videre prosessering av melding")
                oppgaveService.opprett(oppgave)
            }
            persongrunnlagRepo.updateStatus(retry)
        }
    }

    override fun hentOgLås(antall: Int): PersongrunnlagRepo.Locked {
        return persongrunnlagRepo.finnNesteMeldingerForBehandling(antall)
    }


    private fun behandleIntern(melding: PersongrunnlagMelding.Mottatt): FullførteBehandlinger {
        return FullførteBehandlinger(
            behandlinger = omsorgsopptjeningsgrunnlagService.lagOmsorgsopptjeningsgrunnlag(melding)
                .filter { grunnlag -> grunnlag.omsorgsAr == melding.opptjeningsAr }
                .map { it ->
                    behandlingRepo.persist(
                        Behandling(
                            grunnlag = it,
                            vurderVilkår = VilkårsvurderingFactory(
                                grunnlag = it,
                                finnForOmsorgsyterOgÅr = {
                                    behandlingRepo.finnForOmsorgsyterOgAr(
                                        fnr = it.omsorgsyter.fnr,
                                        ar = it.omsorgsAr
                                    ).filterNot { it.erStoppet() }
                                },
                                finnForOmsorgsmottakerOgÅr = {
                                    behandlingRepo.finnForOmsorgsmottakerOgAr(
                                        omsorgsmottaker = it.omsorgsmottaker.fnr,
                                        ar = it.omsorgsAr
                                    ).filterNot { it.erStoppet() }
                                },
                                finnForOmsorgsytersAndreBarnOgÅr = {
                                    behandlingRepo.finnForOmsorgsytersAndreBarn(
                                        omsorgsyter = it.omsorgsyter.fnr,
                                        ar = it.omsorgsAr,
                                        andreBarnEnnOmsorgsmottaker = it.omsorgsyter.finnAndreBarnEnn(it.omsorgsmottaker.fnr)
                                            .map { it.ident }
                                    ).filterNot { it.erStoppet() }
                                }
                            ),
                            meldingId = melding.id
                        )
                    )
                }
        ).also {
            //TODO some might slip through due to transactional boundaries and parallel processing in multiple threads (exsisting issue)
            prosesserOmsorgsyterForTilstøtendeFellesbarnPåNyttVedBehov(it)
        }
    }


    private fun håndterInnvilgelse(behandling: FullførtBehandling) {
        godskrivOpptjeningService.opprett(behandling.godskrivOpptjening())
        brevService.opprettHvisNødvendig(behandling)
    }

    override fun avsluttMelding(id: UUID, melding: String): UUID {
        try {
            return transactionTemplate.execute {
                persongrunnlagRepo.find(id).avsluttet(melding = melding).let {
                    persongrunnlagRepo.updateStatus(it)
                    id
                }
            }
        } catch (ex: Throwable) {
            log.warn("Exception ved avslutting av melding id=$id: ${ex::class.qualifiedName}")
            throw RuntimeException("Kunne ikke oppdatere status")
        }

    }

    /**
     * Identify scenario where two parents receive barnetrygd for separate fellesbarn and send both to manual processing.
     * The scenario is not identifiable until the second omsorgsyter is processed, as we only know about barnetrygd for
     * children that the omsorgsyter currently has barnetrygd for before processing begins.
     * Whenever this scenario occur, we complete the processing of the omsorgsyter currently being processed, as its
     * status will cause oppgave to be created. The other omsorgsyter will have its [PersongrunnlagMelding] stopped
     * and re-processed such that its [OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering] will discover that
     * another omsorgsyter has barnetrygd for children of which it does not receive barnetrygd for itself.
     *
     * @see [OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering]
     * @see [Oppgave.annenForelderInnvilgetOmsorgsopptjeningForAnnetFellesbarn]
     */
    private fun prosesserOmsorgsyterForTilstøtendeFellesbarnPåNyttVedBehov(fullførteBehandlinger: FullførteBehandlinger) {
        when (fullførteBehandlinger.aggregertUtfall) {
            AggregertBehandlingUtfall.Manuell -> {
                fullførteBehandlinger.alle()
                    .filter { it.erManuell() && it.vilkårsvurdering.erUbestemt<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering>() }
                    .map { it.vilkårsvurdering.finnVurdering<OmsorgsopptjeningIkkeInnvilgetAnnetFellesbarn.Vurdering>() }
                    .flatMap { it.grunnlag.behandlinger.map { it.behandlingsId } }
                    .map { behandlingRepo.finn(it) }
                    .filter { it.erInnvilget() && !it.erStoppet() }
                    .map { it.meldingId }
                    .toSet()
                    .forEach {
                        stoppOgOpprettKopiAvMelding(it, "Annen forelder mottar barnetrygd for fellesbarn")
                    }
            }

            else -> {
                //NOOP only applicable for the specific scnario
            }
        }
    }

    private fun stoppMeldingIntern(id: UUID, begrunnelse: String?): UUID {
        log.info("Stopper melding: $id")
        persongrunnlagRepo.find(id).stoppet(begrunnelse = begrunnelse).let {
            persongrunnlagRepo.updateStatus(it)
        }
        oppgaveService.stoppForMelding(
            meldingsId = id,
            begrunnelse = begrunnelse
        )
        brevService.stoppForMelding(
            meldingsId = id,
            begrunnelse = begrunnelse
        )

        behandlingRepo.finnForMelding(id).forEach { fullført ->
            fullført.stoppet(begrunnelse = begrunnelse ?: "").also {
                behandlingRepo.updateStatus(it)
            }
        }

        godskrivOpptjeningService.stoppForMelding(
            meldingsId = id,
            begrunnelse = begrunnelse,
        )
        return id
    }

    private fun opprettKopiAvStoppetMelding(meldingId: UUID): UUID? {
        log.info("Oppretter kopi av melding: $meldingId")
        val gammelMelding = persongrunnlagRepo.tryFind(meldingId)
        when (val status = gammelMelding?.status) {
            null -> throw RuntimeException("Fant ikke melding i databasen: $meldingId")
            is PersongrunnlagMelding.Status.Stoppet -> {
                gammelMelding
            }

            else -> {
                throw RuntimeException("Gammel melding har status: ${status::class.simpleName}")
            }
        }.let {
            PersongrunnlagMelding.Lest(
                innhold = it.innhold,
                opprettet = Instant.now(),
                kopiertFra = it,
            )
        }.let {
            return persongrunnlagRepo.lagre(it)
        }
    }

    override fun rekjørStoppetMelding(meldingsId: UUID): UUID? {
        return transactionTemplate.execute {
            opprettKopiAvStoppetMelding(meldingsId)
        }
    }

    override fun stoppOgOpprettKopiAvMelding(meldingId: UUID, begrunnelse: String?): UUID? {
        try {
            return transactionTemplate.execute {
                stoppMeldingIntern(meldingId, begrunnelse)
                opprettKopiAvStoppetMelding(meldingId)
            }
        } catch (ex: Throwable) {
            secureLog.warn("Fikk feil ved stopp og opprettelse av  melding", ex)
            throw ex
        }
    }

    override fun stoppMelding(id: UUID, begrunnelse: String?): UUID {
        return transactionTemplate.execute {
            stoppMeldingIntern(id, begrunnelse)
        }
    }

    override fun frigi(locked: PersongrunnlagRepo.Locked) {
        return persongrunnlagRepo.frigi(locked)
    }
}


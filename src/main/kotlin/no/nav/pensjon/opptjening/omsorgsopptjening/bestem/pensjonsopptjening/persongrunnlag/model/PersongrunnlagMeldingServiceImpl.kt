package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
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
    private val gyldigOpptjeningsår: GyldigOpptjeningår,
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
                .filter { grunnlag -> gyldigOpptjeningsår.erGyldig(grunnlag.omsorgsAr) }
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
                                    )
                                },
                                finnForOmsorgsmottakerOgÅr = {
                                    behandlingRepo.finnForOmsorgsmottakerOgAr(
                                        omsorgsmottaker = it.omsorgsmottaker.fnr,
                                        ar = it.omsorgsAr
                                    )
                                },
                                finnForOmsorgsytersAndreBarnOgÅr = {
                                    behandlingRepo.finnForOmsorgsytersAndreBarn(
                                        omsorgsyter = it.omsorgsyter.fnr,
                                        ar = it.omsorgsAr,
                                        andreBarnEnnOmsorgsmottaker = it.omsorgsyter.finnAndreBarnEnn(it.omsorgsmottaker.fnr)
                                            .map { it.ident }
                                    )
                                }
                            ),
                            meldingId = melding.id
                        )
                    )
                }
        )
    }


    private fun håndterInnvilgelse(behandling: FullførtBehandling) {
        godskrivOpptjeningService.opprett(behandling.godskrivOpptjening())
        brevService.opprettHvisNødvendig(behandling)
    }

    override fun avsluttMelding(id: UUID, melding: String): UUID? {
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
        behandlingRepo.stoppBehandlingerForMelding(
            meldingsId = id
        )
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

    override fun stoppMelding(id: UUID, begrunnelse: String?): UUID? {
        return transactionTemplate.execute {
            stoppMeldingIntern(id, begrunnelse)
        }
    }

    override fun frigi(locked: PersongrunnlagRepo.Locked) {
        return persongrunnlagRepo.frigi(locked)
    }
}


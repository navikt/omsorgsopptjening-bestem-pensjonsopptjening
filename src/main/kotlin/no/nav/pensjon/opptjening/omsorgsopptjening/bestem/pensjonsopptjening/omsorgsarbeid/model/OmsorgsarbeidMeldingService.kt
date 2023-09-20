package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.tilOmsorgsopptjeningsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class OmsorgsarbeidMeldingService(
    private val behandlingRepo: BehandlingRepo,
    private val gyldigOpptjeningsår: GyldigOpptjeningår,
    private val omsorgsarbeidRepo: OmsorgsarbeidRepo,
    private val oppgaveService: OppgaveService,
    private val personOppslag: PersonOppslag,
    private val godskrivOpptjeningService: GodskrivOpptjeningService,
    private val transactionTemplate: TransactionTemplate,
    private val brevService: BrevService,
    private val hentPensjonspoeng: HentPensjonspoengClient,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun process(): List<FullførtBehandling> {
        return transactionTemplate.execute {
            omsorgsarbeidRepo.finnNesteUprosesserte()?.let { melding ->
                Mdc.scopedMdc(melding.correlationId) {
                    Mdc.scopedMdc(melding.innlesingId) {
                        try {
                            transactionTemplate.execute {
                                log.info("Prosesserer melding")
                                handle(melding).also { resultat ->
                                    omsorgsarbeidRepo.updateStatus(melding.ferdig())
                                    resultat.forEach {
                                        when (it.erInnvilget()) {
                                            true -> {
                                                håndterInnvilgelse(it)
                                            }

                                            false -> {
                                                håndterAvslag(it)
                                            }
                                        }
                                        log.info("Melding prosessert")
                                    }
                                }
                            }
                        } catch (ex: Throwable) {
                            transactionTemplate.execute {
                                melding.retry(ex.toString()).let { melding ->
                                    melding.opprettOppgave()?.let {
                                        log.error("Gir opp videre prosessering av melding")
                                        oppgaveService.opprett(it)
                                    }
                                    omsorgsarbeidRepo.updateStatus(melding)
                                }
                            }
                            throw ex
                        }
                    }
                }
            }
        } ?: emptyList()
    }

    private fun handle(melding: OmsorgsarbeidMelding.Mottatt): List<FullførtBehandling> {
        return melding.innhold
            .berikDatagrunnlag()
            .tilOmsorgsopptjeningsgrunnlag()
            .filter { grunnlag ->
                gyldigOpptjeningsår.get().contains(grunnlag.omsorgsAr).also {
                    if (!it) log.info("Filtrerer vekk grunnlag for ugyldig opptjeningsår: ${grunnlag.omsorgsAr}")
                }
            }
            .map {
                log.info("Utfører vilkårsvurdering")
                behandlingRepo.persist(
                    Behandling(
                        grunnlag = it,
                        vurderVilkår = VilkårsvurderingFactory(
                            grunnlag = it,
                            behandlingRepo = behandlingRepo
                        ),
                        meldingId = melding.id
                    )
                )
            }
    }


    private fun håndterInnvilgelse(behandling: FullførtBehandling) {
        log.info("Håndterer innvilgelse")
        godskrivOpptjeningService.opprett(behandling.godskrivOpptjening())
        behandling.sendBrev(
            hentPensjonspoengForOmsorgsopptjening = hentPensjonspoeng::hentPensjonspoengForOmsorgsarbeid,
            hentPensjonspoengForInntekt = hentPensjonspoeng::hentPensjonspoengForInntekt,
        )?.also { brevService.opprett(it) }
    }

    private fun håndterAvslag(behandling: FullførtBehandling) {
        log.info("Håndterer avslag")
        behandling.opprettOppgave(
            oppgaveEksistererForOmsorgsyter = oppgaveService::oppgaveEksistererForOmsorgsyterOgÅr,
            oppgaveEksistererForOmsorgsmottaker = oppgaveService::oppgaveEksistererForOmsorgsmottakerOgÅr
        )?.also { oppgaveService.opprett(it) }
    }

    private fun OmsorgsgrunnlagMelding.berikDatagrunnlag(): BeriketDatagrunnlag {
        val personer = hentPersoner()
            .map { personOppslag.hentPerson(it) }
            .toSet()

        return berikDatagrunnlag(personer)
    }

    private fun OmsorgsgrunnlagMelding.berikDatagrunnlag(persondata: Set<Person>): BeriketDatagrunnlag {
        fun Set<Person>.finnPerson(fnr: String): Person {
            return single { it.fnr == fnr }
        }

        return BeriketDatagrunnlag(
            omsorgsyter = persondata.finnPerson(omsorgsyter),
            omsorgsSaker = saker.map { omsorgsSak ->
                BeriketSak(
                    omsorgsyter = persondata.finnPerson(omsorgsSak.omsorgsyter),
                    omsorgVedtakPerioder = omsorgsSak.vedtaksperioder.map { omsorgVedtakPeriode ->
                        BeriketVedtaksperiode(
                            fom = omsorgVedtakPeriode.fom,
                            tom = omsorgVedtakPeriode.tom,
                            omsorgstype = omsorgVedtakPeriode.omsorgstype.toDomain(),
                            omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker)
                        )
                    }

                )
            },
            innlesingId = innlesingId,
            correlationId = correlationId
        )
    }
}
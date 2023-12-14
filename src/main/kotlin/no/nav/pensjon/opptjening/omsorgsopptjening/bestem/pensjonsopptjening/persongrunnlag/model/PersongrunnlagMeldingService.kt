package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.util.*
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

@Service
class PersongrunnlagMeldingService(
    private val behandlingRepo: BehandlingRepo,
    private val gyldigOpptjeningsår: GyldigOpptjeningår,
    private val persongrunnlagRepo: PersongrunnlagRepo,
    private val oppgaveService: OppgaveService,
    private val personOppslag: PersonOppslag,
    private val godskrivOpptjeningService: GodskrivOpptjeningService,
    private val transactionTemplate: TransactionTemplate,
    private val brevService: BrevService,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun process(): List<FullførteBehandlinger>? {
        val lockId = UUID.randomUUID()
        val meldinger = transactionTemplate.execute {
            persongrunnlagRepo.finnNesteMeldingerForBehandling(10)
        }!!
        try {
            return meldinger.data.mapNotNull { melding ->
                Mdc.scopedMdc(melding.correlationId) {
                    Mdc.scopedMdc(melding.innlesingId) {
                        try {
                            log.info("Started behandling av melding $melding")
                            transactionTemplate.execute {
                                behandle(melding).let { fullførte ->
                                    persongrunnlagRepo.updateStatus(melding.ferdig())
                                    fullførte.also {
                                        it.håndterUtfall(
                                            innvilget = ::håndterInnvilgelse,
                                            manuell = oppgaveService::opprettOppgaveHvisNødvendig,
                                            avslag = {} //noop
                                        )
                                        log.info("Melding prosessert")
                                    }
                                }
                            }
                        } catch (ex: Throwable) {
                            transactionTemplate.execute {
                                melding.retry(ex.stackTraceToString()).let { melding ->
                                    melding.opprettOppgave()?.let {
                                        log.error("Gir opp videre prosessering av melding")
                                        oppgaveService.opprett(it)
                                    }
                                    persongrunnlagRepo.updateStatus(melding)
                                }
                            }
                            null
                        } finally {
                            log.info("Avsluttet behandling av melding")
                        }
                    }
                }
            }
        } catch (ex: Throwable) {
            log.error("Fikk exception ved uthenting av meldinger: ${ex::class.qualifiedName}")
            return null // throw ex
        } finally {
            transactionTemplate.execute {
                persongrunnlagRepo.frigi(meldinger)
            }
        }
    }


    private fun behandle(melding: PersongrunnlagMelding.Mottatt): FullførteBehandlinger {
        return FullførteBehandlinger(
            behandlinger = melding.innhold
                .berikDatagrunnlag()
                .tilOmsorgsopptjeningsgrunnlag()
                .filter { grunnlag -> gyldigOpptjeningsår.erGyldig(grunnlag.omsorgsAr) }
                .map {
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
        )
    }


    private fun håndterInnvilgelse(behandling: FullførtBehandling) {
        godskrivOpptjeningService.opprett(behandling.godskrivOpptjening())
        brevService.opprettHvisNødvendig(behandling)
    }

    private fun PersongrunnlagMeldingKafka.berikDatagrunnlag(): BeriketDatagrunnlag {
        val personer = hentPersoner()
            .map { personOppslag.hentPerson(it) }
            .toSet()

        return berikDatagrunnlag(personer)
    }

    private fun PersongrunnlagMeldingKafka.berikDatagrunnlag(persondata: Set<Person>): BeriketDatagrunnlag {
        fun Set<Person>.finnPerson(fnr: String): Person {
            return singleOrNull { it.identifisertAv(fnr) } ?: throw PersonIkkeIdentifisertAvIdentException()
        }

        return BeriketDatagrunnlag(
            omsorgsyter = persondata.finnPerson(omsorgsyter),
            persongrunnlag = persongrunnlag.map { persongrunnlag ->
                val omsorgsyter = persondata.finnPerson(persongrunnlag.omsorgsyter)
                Persongrunnlag(
                    omsorgsyter = omsorgsyter,
                    omsorgsperioder = persongrunnlag.omsorgsperioder.map { omsorgVedtakPeriode ->
                        Omsorgsperiode(
                            fom = omsorgVedtakPeriode.fom,
                            tom = omsorgVedtakPeriode.tom,
                            omsorgstype = omsorgVedtakPeriode.omsorgstype.toDomain(),
                            omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker),
                            kilde = omsorgVedtakPeriode.kilde.toDomain(),
                            utbetalt = omsorgVedtakPeriode.utbetalt,
                            landstilknytning = omsorgVedtakPeriode.landstilknytning.toDomain()
                        )
                    },
                    hjelpestønadperioder = persongrunnlag.hjelpestønadsperioder.map { hjelpestønadperiode ->
                        Hjelpestønadperiode(
                            fom = hjelpestønadperiode.fom,
                            tom = hjelpestønadperiode.tom,
                            omsorgstype = hjelpestønadperiode.omsorgstype.toDomain(),
                            omsorgsmottaker = persondata.finnPerson(hjelpestønadperiode.omsorgsmottaker),
                            kilde = hjelpestønadperiode.kilde.toDomain()
                        )
                    }
                )
            },
            innlesingId = innlesingId,
            correlationId = correlationId
        )
    }

    class PersonIkkeIdentifisertAvIdentException(msg: String = "Person kunne ikke identifiseres av oppgitt ident") :
        RuntimeException(msg)
}


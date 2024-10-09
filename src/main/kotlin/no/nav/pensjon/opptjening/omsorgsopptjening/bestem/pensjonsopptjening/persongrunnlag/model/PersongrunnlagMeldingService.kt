package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.medlemskap.MedlemskapOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Behandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Medlemskapsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.VilkårsvurderingFactory
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.tilOmsorgsopptjeningsgrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.Mdc
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.KanSlåsSammen.Companion.slåSammenLike
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.util.UUID
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag as KafkaPersongrunnlag


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
    private val medlemskapOppslag: MedlemskapOppslag
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private val secureLog: Logger = LoggerFactory.getLogger("secure")
    }

    fun process(): List<FullførteBehandlinger>? {
        val meldinger = transactionTemplate.execute {
            persongrunnlagRepo.finnNesteMeldingerForBehandling(10)
        }!!
        try {
            return meldinger.data.mapNotNull { melding ->
                Mdc.scopedMdc(melding.correlationId) {
                    Mdc.scopedMdc(melding.innlesingId) {
                        try {
                            log.info("Started behandling av melding")
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
                            // TODO: SQLException og andre tekniske feil bør ikke medføre retry, kun rollback
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

    private fun consolidatePersongrunnlag(
        key: (KafkaPersongrunnlag) -> Any,
        persongrunnlag: List<KafkaPersongrunnlag>
    ): List<KafkaPersongrunnlag> {
        fun merge(persongrunnlag: List<KafkaPersongrunnlag>): KafkaPersongrunnlag {
            // todo : sjekk omsorgsperioder
            val omsorgsyter = persongrunnlag.first().omsorgsyter
            val omsorgsperioder = persongrunnlag.flatMap { it.omsorgsperioder }.sortedBy { it.fom }.slåSammenLike()
            val hjelpestønadperioder =
                persongrunnlag.flatMap { it.hjelpestønadsperioder }.sortedBy { it.fom }.slåSammenLike()
            return KafkaPersongrunnlag(
                omsorgsyter = omsorgsyter,
                omsorgsperioder = omsorgsperioder,
                hjelpestønadsperioder = hjelpestønadperioder,
            )
        }

        val persongrunnlag = persongrunnlag.groupBy { key(it) }.values.map { merge(it) }
        return persongrunnlag
    }

    private fun List<KafkaPersongrunnlag>.consolidate(key: (KafkaPersongrunnlag) -> Any): List<KafkaPersongrunnlag> {
        return consolidatePersongrunnlag(key, this)
    }

    private fun PersongrunnlagMeldingKafka.berikDatagrunnlag(persondata: Set<Person>): BeriketDatagrunnlag {
        fun Set<Person>.finnPerson(fnr: String): Person {
            return singleOrNull { it.identifisertAv(fnr) } ?: throw PersonIkkeIdentifisertAvIdentException()
        }

        return BeriketDatagrunnlag(
            omsorgsyter = persondata.finnPerson(omsorgsyter),
            persongrunnlag = persongrunnlag.consolidate { persondata.finnPerson(it.omsorgsyter) }
                .map { persongrunnlag ->
                    val omsorgsyter = persondata.finnPerson(persongrunnlag.omsorgsyter)

                    val omsorgsperioder = persongrunnlag.omsorgsperioder.map { omsorgVedtakPeriode ->
                        Omsorgsperiode(
                            fom = omsorgVedtakPeriode.fom,
                            tom = omsorgVedtakPeriode.tom,
                            omsorgstype = omsorgVedtakPeriode.omsorgstype.toDomain() as DomainOmsorgstype.Barnetrygd,
                            omsorgsmottaker = persondata.finnPerson(omsorgVedtakPeriode.omsorgsmottaker),
                            kilde = omsorgVedtakPeriode.kilde.toDomain(),
                            utbetalt = omsorgVedtakPeriode.utbetalt,
                            landstilknytning = omsorgVedtakPeriode.landstilknytning.toDomain()
                        )
                    }

                    val hjelpestønadsperioder = persongrunnlag.hjelpestønadsperioder.map { hjelpestønadperiode ->
                        Hjelpestønadperiode(
                            fom = hjelpestønadperiode.fom,
                            tom = hjelpestønadperiode.tom,
                            omsorgstype = hjelpestønadperiode.omsorgstype.toDomain() as DomainOmsorgstype.Hjelpestønad,
                            omsorgsmottaker = persondata.finnPerson(hjelpestønadperiode.omsorgsmottaker),
                            kilde = hjelpestønadperiode.kilde.toDomain()
                        )
                    }

                    val medlemskapsgrunnlag = if (omsorgsperioder.isNotEmpty()) {
                        val (første, siste) = omsorgsperioder.minOf { it.fom } to omsorgsperioder.maxOf { it.tom }
                        medlemskapOppslag.hentMedlemskapsgrunnlag(
                            fnr = omsorgsyter.fnr,
                            fraOgMed = første,
                            tilOgMed = siste,
                        )
                    } else {
                        Medlemskapsgrunnlag(emptyList(), "")
                    }

                    Persongrunnlag(
                        omsorgsyter = omsorgsyter,
                        omsorgsperioder = omsorgsperioder,
                        hjelpestønadperioder = hjelpestønadsperioder,
                        medlemskapsgrunnlag = medlemskapsgrunnlag,
                    )
                },
            innlesingId = innlesingId,
            correlationId = correlationId
        )
    }

    fun avsluttMelding(id: UUID, melding: String): UUID? {
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
        persongrunnlagRepo.find(id).stoppet(begrunnelse).let {
            persongrunnlagRepo.updateStatus(it)
        }
        oppgaveService.stoppForMelding(meldingsId = id)
        brevService.stoppForMelding(meldingsId = id)
        behandlingRepo.stoppBehandlingerForMelding(meldingsId = id)
        godskrivOpptjeningService.stoppForMelding(meldingsId = id)
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
                kopiertFra = it
            )
        }.let {
            return persongrunnlagRepo.lagre(it)
        }
    }

    fun rekjørStoppetMelding(meldingsId: UUID): UUID? {
        return transactionTemplate.execute {
            opprettKopiAvStoppetMelding(meldingsId)
        }
    }

    fun stoppOgOpprettKopiAvMelding(meldingId: UUID, begrunnelse: String? = null): UUID? {
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

    fun stoppMelding(id: UUID, begrunnelse: String? = null): UUID? {
        return transactionTemplate.execute {
            stoppMeldingIntern(id, begrunnelse)
        }
    }

    class PersonIkkeIdentifisertAvIdentException(msg: String = "Person kunne ikke identifiseres av oppgitt ident") :
        RuntimeException(msg)
}


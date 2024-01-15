package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjening
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BrevÅrsak
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month.*
import java.time.YearMonth
import java.util.*

class AdministrasjonsTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @Autowired
    private lateinit var brevService: BrevService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår


    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
        const val OPPTJENINGSÅR = 2020
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        willAnswer { true }
            .given(gyldigOpptjeningår).erGyldig(OPPTJENINGSÅR)
    }

    fun lagreOgProsesserMeldingSomGirBrev(): UUID {
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.givenThat(
            WireMock.get(WireMock.urlPathEqualTo(POPP_PENSJONSPOENG_PATH))
                .withHeader("fnr", WireMock.equalTo("04010012797")) //far
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "pensjonspoeng": [
                                    {
                                        "ar":$OPPTJENINGSÅR,
                                        "poeng":7.4,
                                        "pensjonspoengType":"PPI"
                                    }
                                ]
                            }
                        """.trimIndent()
                        )
                )
        )

        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Omsorgsperiode(
                                    fom = YearMonth.of(2018, JANUARY),
                                    tom = YearMonth.of(2030, DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, JANUARY),
                                    tom = YearMonth.of(2030, DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.INFOTRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process()!!.first().single().also { behandling ->
            Assertions.assertTrue(behandling.erInnvilget())

            Assertions.assertInstanceOf(
                Brev::class.java,
                brevRepository.findForBehandling(behandling.id).singleOrNull()
            ).also {
                assertThat(it.årsak).isEqualTo(BrevÅrsak.OMSORGSYTER_INGEN_PENSJONSPOENG_FORRIGE_ÅR)
            }
        }
        return melding!!
    }


    fun lagreOgProsesserMeldingSomGirOppgave(): UUID {
        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Omsorgsperiode(
                                    fom = YearMonth.of(2021, JANUARY),
                                    tom = YearMonth.of(2021, JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Omsorgsperiode(
                                    fom = YearMonth.of(2021, JULY),
                                    tom = YearMonth.of(2021, DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process()!!.first().single().let { behandling ->
            assertThat(behandling.utfall).isInstanceOf(BehandlingUtfall.Manuell::class.java)
            assertThat(
                behandling.hentOppgaveopplysninger()
            ).hasSize(1)
        }
        return melding!!
    }

    @Test
    fun `kan avslutte en transaksjon`() {
        val meldingsId = lagreOgProsesserMeldingSomGirOppgave()
        handler.avsluttMelding(meldingsId)
        repo.find(meldingsId).let { melding ->
            assertThat(melding.status).isInstanceOf(PersongrunnlagMelding.Status.Avsluttet::class.java)
        }
    }

    @Test
    fun `stopping av melding stopper også oppgave`() {
        val meldingsId = lagreOgProsesserMeldingSomGirOppgave()
        handler.stoppMelding(meldingsId)
        repo.find(meldingsId).let { melding ->
            assertThat(melding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
        }
        oppgaveRepo.findForMelding(meldingsId).let { oppgaver ->
            val oppgave = oppgaver.single()
            assertThat(oppgave.status).isInstanceOf(Oppgave.Status.Stoppet::class.java)
        }
        assertThat(brevRepository.findForMelding(meldingsId)).isEmpty()
    }

    @Test
    fun `stoppet melding har lagret beskrivelse`() {
        val begrunnelse = "Fordi jeg vil!"
        val meldingsId = lagreOgProsesserMeldingSomGirOppgave()
        handler.stoppMelding(meldingsId,begrunnelse)
        repo.find(meldingsId).let { melding ->
            assertThat(melding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
            (melding.status as PersongrunnlagMelding.Status.Stoppet).let {
                assertThat(it.begrunnelse).isEqualTo(begrunnelse)
            }
        }
        oppgaveRepo.findForMelding(meldingsId).let { oppgaver ->
            val oppgave = oppgaver.single()
            assertThat(oppgave.status).isInstanceOf(Oppgave.Status.Stoppet::class.java)
        }
        assertThat(brevRepository.findForMelding(meldingsId)).isEmpty()
    }


    @Test
    fun `stopping av melding stopper også brev`() {
        val meldingsId = lagreOgProsesserMeldingSomGirBrev()
        handler.stoppMelding(meldingsId)
        repo.find(meldingsId).let { melding ->
            assertThat(melding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
        }
        assertThat(oppgaveRepo.findForMelding(meldingsId)).isEmpty()
        brevRepository.findForMelding(meldingsId).let {
            val brev = it.single()
            assertThat(brev.status).isInstanceOf(Brev.Status.Stoppet::class.java)
        }
    }

    @Test
    fun `stopping av melding stopper også godskriving`() {
        val meldingsId = lagreOgProsesserMeldingSomGirBrev()
        handler.stoppMelding(meldingsId)
        repo.find(meldingsId).let { melding ->
            assertThat(melding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
        }
        assertThat(oppgaveRepo.findForMelding(meldingsId)).isEmpty()
        godskrivOpptjeningRepo.findForMelding(meldingsId).let {
            val godskriving = it.single()
            assertThat(godskriving.status).isInstanceOf(GodskrivOpptjening.Status.Stoppet::class.java)
        }
    }

    @Test
    fun `kan kopiere og rekjøre melding med oppgave`() {
        val stoppetmeldingId = lagreOgProsesserMeldingSomGirOppgave().let {
            handler.stoppMelding(it)!!
        }
        val nyMelding =
            handler.rekjørStoppetMelding(stoppetmeldingId)!!.let {
                repo.find(it)
            }
        val stoppetMelding = repo.find(stoppetmeldingId)
        val stoppetOppgave = oppgaveRepo.findForMelding(stoppetmeldingId)!!.single()

        val behandling = handler.process()!!.single()
        val nyOppgave = oppgaveRepo.findForBehandling(behandling.alle().single().id).single()

        assertThat(stoppetMelding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
        assertThat(stoppetOppgave.status).isInstanceOf(Oppgave.Status.Stoppet::class.java)

        assertThat(nyMelding.status).isInstanceOf(PersongrunnlagMelding.Status.Klar::class.java)
        assertThat(nyOppgave.status).isInstanceOf(Oppgave.Status.Klar::class.java)
    }

    @Test
    fun `kan kopiere og rekjøre melding med brev`() {
        val stoppetMeldingId = lagreOgProsesserMeldingSomGirBrev().let {
            handler.stoppMelding(it)!!
        }
        val nyMelding =
            handler.rekjørStoppetMelding(stoppetMeldingId)!!.let {
                repo.find(it)
            }
        val stoppetMelding = repo.find(stoppetMeldingId)
        val stoppetBrev = brevRepository.findForMelding(stoppetMeldingId)!!.single()

        val behandling = handler.process()!!.single()

        val nyttBrev = brevRepository.findForBehandling(behandling.alle().single().id).single()

        assertThat(stoppetMelding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
        assertThat(stoppetBrev.status).isInstanceOf(Brev.Status.Stoppet::class.java)

        assertThat(nyMelding.status).isInstanceOf(PersongrunnlagMelding.Status.Klar::class.java)
        assertThat(nyttBrev.status).isInstanceOf(Brev.Status.Klar::class.java)
    }

    @Test
    fun `kan kopiere og rekjøre melding med godskriving`() {
        val stoppetMeldingId = lagreOgProsesserMeldingSomGirBrev().let {
            handler.stoppMelding(it)!!
        }
        val nyMelding =
            handler.rekjørStoppetMelding(stoppetMeldingId)!!.let {
                repo.find(it)
            }
        val stoppetMelding = repo.find(stoppetMeldingId)
        val stoppetGodskriv = godskrivOpptjeningRepo.findForMelding(stoppetMeldingId)!!.single()

        val behandling = handler.process()!!.single()

        val nyGodskriv = godskrivOpptjeningRepo.findForBehandling(behandling.alle().single().id).single()

        assertThat(stoppetMelding.status).isInstanceOf(PersongrunnlagMelding.Status.Stoppet::class.java)
        assertThat(stoppetGodskriv.status).isInstanceOf(GodskrivOpptjening.Status.Stoppet::class.java)

        assertThat(nyMelding.status).isInstanceOf(PersongrunnlagMelding.Status.Klar::class.java)
        assertThat(nyGodskriv.status).isInstanceOf(GodskrivOpptjening.Status.Klar::class.java)
    }


    @Test
    fun `kan restarte oppgave`() {
        val oppgaveId = lagreOgProsesserMeldingSomGirOppgave().let { meldingId ->
            oppgaveRepo.findForMelding(meldingId).single()
        }.retry("1").retry("2").retry("4").retry("feiler").let { oppgave ->
            assertThat(oppgave.status).isInstanceOf(Oppgave.Status.Feilet::class.java)
            oppgaveRepo.updateStatus(oppgave)
            oppgave.id
        }
        assertThat(oppgaveRepo.find(oppgaveId).status).isInstanceOf(Oppgave.Status.Feilet::class.java)

        val id = oppgaveService.restart(oppgaveId)!!
        assertThat(id).isEqualTo(oppgaveId)

        oppgaveRepo.find(oppgaveId).let { oppgave ->
            assertThat(oppgave.status).isInstanceOf(Oppgave.Status.Klar::class.java)
            assertThat(oppgave.status.retry("etter start")).isInstanceOf(Oppgave.Status.Retry::class.java)
        }
    }

    @Test
    fun `kan restarte brev`() {
        val brevId = lagreOgProsesserMeldingSomGirBrev().let { meldingId ->
            brevRepository.findForMelding(meldingId).single()
        }.retry("1").retry("2").retry("4").retry("feiler").let { brev ->
            assertThat(brev.status).isInstanceOf(Brev.Status.Feilet::class.java)
            brevRepository.updateStatus(brev)
            brev.id
        }
        assertThat(brevRepository.find(brevId).status).isInstanceOf(Brev.Status.Feilet::class.java)

        val id = brevService.restart(brevId)!!
        assertThat(id).isEqualTo(brevId)

        brevRepository.find(brevId).let { brev ->
            assertThat(brev.status).isInstanceOf(Brev.Status.Klar::class.java)
            assertThat(brev.status.retry("etter start")).isInstanceOf(Brev.Status.Retry::class.java)
        }
    }
}
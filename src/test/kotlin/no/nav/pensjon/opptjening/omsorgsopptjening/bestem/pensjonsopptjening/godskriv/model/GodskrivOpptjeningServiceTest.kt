package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.external.PoppClientExecption
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class GodskrivOpptjeningServiceTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    @Autowired
    private lateinit var godskrivOpptjeningService: GodskrivOpptjeningService

    @MockBean
    private lateinit var clock: Clock

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `gitt at en rad feiler, så skal den kunne retryes og gå bra på et senere tidspunkt`() {
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(WireMock.serverError())
                .willSetStateTo("ok")
        )
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(WireMock.ok())
        )

        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        /**
         * Stiller klokka litt fram i tid for å unngå at [GodskrivOpptjening.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))

        val melding = repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process()!!.single().also { behandling ->
            godskrivOpptjeningRepo.finnNesteUprosesserte()!!.also {
                assertInstanceOf(GodskrivOpptjening.Status.Klar::class.java, it.status)
                assertEquals(behandling.id, it.behandlingId)
                assertEquals(melding.correlationId, it.correlationId)
                assertEquals(melding.innlesingId, it.innlesingId)
                assertEquals(behandling.omsorgsyter, it.omsorgsyter)
            }

            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }

            godskrivOpptjeningRepo.finnNesteUprosesserte()!!.also {
                assertInstanceOf(GodskrivOpptjening.Status.Retry::class.java, it.status).also {
                    assertEquals(1, it.antallForsøk)
                    assertEquals(3, it.maxAntallForsøk)
                    assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                }
            }

            assertInstanceOf(GodskrivOpptjening.Persistent::class.java, godskrivOpptjeningService.process()).also {
                assertInstanceOf(GodskrivOpptjening.Status.Ferdig::class.java, it.status)
            }
        }
    }

    @Test
    fun `gitt at prosesseringen ender med retry, havner den aktuelle raden i karantene før den forsøkes på nytt igjen`() {
        wiremock.stubForPdlTransformer()
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(WireMock.serverError())
                .willSetStateTo("ok")
        )
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(WireMock.ok())
        )
        given(clock.instant()).willReturn(
            Clock.systemUTC().instant(), //karantene -- handler kalles
            Clock.systemUTC().instant(), //karantene
            Clock.systemUTC().instant().plus(2, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(4, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(6, ChronoUnit.HOURS), //karantenetid utløpt
        )

        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        val melding = repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process()!!.single().also {
            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }

            assertNull(godskrivOpptjeningService.process())
            assertNull(godskrivOpptjeningService.process())
            assertNotNull(godskrivOpptjeningService.process())

            assertInstanceOf(
                GodskrivOpptjening.Persistent::class.java,
                godskrivOpptjeningRepo.findForMelding(melding.id).single()
            ).also {
                assertInstanceOf(GodskrivOpptjening.Status.Ferdig::class.java, it.status)
            }
        }
    }

    @Test
    fun `gitt at en oppgave har blitt forsøkt opprettet maks antall ganger uten hell får den status feilet og oppgave opprettes`() {
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(WireMock.serverError())
        )
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        /**
         * Stiller klokka litt fram i tid for å unngå at [GodskrivOpptjening.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))

        val melding = repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process()!!.single().also { behandling ->
            godskrivOpptjeningRepo.finnNesteUprosesserte()!!.also {
                assertInstanceOf(GodskrivOpptjening.Status.Klar::class.java, it.status)
                assertEquals(behandling.id, it.behandlingId)
                assertEquals(melding.correlationId, it.correlationId)
                assertEquals(melding.innlesingId, it.innlesingId)
                assertEquals(behandling.omsorgsyter, it.omsorgsyter)
            }

            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }

            godskrivOpptjeningRepo.finnNesteUprosesserte()!!.also {
                assertInstanceOf(GodskrivOpptjening.Status.Retry::class.java, it.status).also {
                    assertEquals(1, it.antallForsøk)
                    assertEquals(3, it.maxAntallForsøk)
                    assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                }
            }

            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }
            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }
            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }

            godskrivOpptjeningRepo.findForBehandling(behandling.id).single().also { godskrivOpptjening ->
                assertInstanceOf(GodskrivOpptjening.Status.Feilet::class.java, godskrivOpptjening.status)

                assertInstanceOf(
                    Oppgave::class.java,
                    oppgaveRepo.findForBehandling(behandling.id).single()
                ).also { oppgave ->
                    assertEquals(
                        OppgaveDetaljer.MottakerOgTekst(
                            oppgavemottaker = behandling.omsorgsyter,
                            oppgavetekst = """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."""
                        ),
                        oppgave.detaljer
                    )
                }
            }
        }
    }
}

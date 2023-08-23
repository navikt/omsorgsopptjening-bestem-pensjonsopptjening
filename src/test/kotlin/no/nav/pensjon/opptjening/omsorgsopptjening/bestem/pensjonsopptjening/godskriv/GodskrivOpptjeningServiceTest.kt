package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
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
import java.util.UUID
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GodskrivOpptjeningServiceTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMeldingService

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
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(WireMock.serverError())
                .willSetStateTo("ok")
        )
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
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

        val correlationId = UUID.randomUUID().toString()

        repo.persist(
            OmsorgsarbeidMelding(
                melding = serialize(
                    OmsorgsgrunnlagMelding(
                        omsorgsyter = "12345678910",
                        omsorgstype = Omsorgstype.BARNETRYGD,
                        kjoreHash = "xxx",
                        kilde = Kilde.BARNETRYGD,
                        saker = listOf(
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "12345678910",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = correlationId,
            )
        )


        handler.process().single().also { behandling ->
            godskrivOpptjeningRepo.finnNesteUprosesserte()!!.also {
                assertInstanceOf(GodskrivOpptjening.Status.Klar::class.java, it.status)
                assertEquals(behandling.id, it.behandlingId)
                assertEquals(correlationId, it.correlationId.toString())
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

            assertInstanceOf(GodskrivOpptjening::class.java, godskrivOpptjeningService.process()).also {
                assertInstanceOf(GodskrivOpptjening.Status.Ferdig::class.java, it.status)
            }
        }
    }

    @Test
    fun `gitt at prosesseringen ender med retry, havner den aktuelle raden i karantene før den forsøkes på nytt igjen`() {
        wiremock.stubForPdlTransformer()
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(WireMock.serverError())
                .willSetStateTo("ok")
        )
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
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

        val correlationId = UUID.randomUUID().toString()

        val melding = repo.persist(
            OmsorgsarbeidMelding(
                melding = serialize(
                    OmsorgsgrunnlagMelding(
                        omsorgsyter = "12345678910",
                        omsorgstype = Omsorgstype.BARNETRYGD,
                        kjoreHash = "xxx",
                        kilde = Kilde.BARNETRYGD,
                        saker = listOf(
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "12345678910",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = correlationId,
            )
        )


        handler.process().single().also { behandling ->
            assertThrows<PoppClientExecption> {
                godskrivOpptjeningService.process()
            }

            assertNull(godskrivOpptjeningService.process())
            assertNull(godskrivOpptjeningService.process())
            assertNotNull(godskrivOpptjeningService.process())

            assertInstanceOf(GodskrivOpptjening::class.java, godskrivOpptjeningRepo.findForMelding(melding.id!!).single()).also {
                assertInstanceOf(GodskrivOpptjening.Status.Ferdig::class.java, it.status)
            }
        }
    }

    @Test
    fun `gitt at en oppgave har blitt forsøkt opprettet maks antall ganger uten hell får den status feilet og oppgave opprettes`() {
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_PATH))
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

        val correlationId = UUID.randomUUID().toString()

        repo.persist(
            OmsorgsarbeidMelding(
                melding = serialize(
                    OmsorgsgrunnlagMelding(
                        omsorgsyter = "12345678910",
                        omsorgstype = Omsorgstype.BARNETRYGD,
                        kjoreHash = "xxx",
                        kilde = Kilde.BARNETRYGD,
                        saker = listOf(
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "12345678910",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = correlationId,
            )
        )


        handler.process().single().also { behandling ->
            godskrivOpptjeningRepo.finnNesteUprosesserte()!!.also {
                assertInstanceOf(GodskrivOpptjening.Status.Klar::class.java, it.status)
                assertEquals(behandling.id, it.behandlingId)
                assertEquals(correlationId, it.correlationId.toString())
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

                assertInstanceOf(Oppgave::class.java, oppgaveRepo.findForBehandling(behandling.id).single()).also { oppgave ->
                    assertInstanceOf(OppgaveDetaljer.UspesifisertFeilsituasjon::class.java, oppgave.detaljer).also {
                        assertEquals(behandling.omsorgsyter, it.omsorgsyter)
                        assertEquals(
                            """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch.""",
                            it.oppgaveTekst
                        )
                    }
                }
            }
        }
    }
}

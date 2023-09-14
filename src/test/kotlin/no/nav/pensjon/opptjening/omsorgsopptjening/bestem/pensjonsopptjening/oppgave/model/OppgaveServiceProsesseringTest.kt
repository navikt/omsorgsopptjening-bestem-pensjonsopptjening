package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.external.BestemSakClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
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
import org.springframework.http.HttpHeaders
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class OppgaveServiceProsesseringTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @MockBean
    private lateinit var clock: Clock

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `gitt at prosesseringen en oppgave feiler, så kan den retryes og gå ok på et senere tidspunkt `() {
        wiremock.stubForPdlTransformer()
        /**
         * Stiller klokka litt fram i tid for å unngå at [Oppgave.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                {
                                    "feil":{
                                        "feilKode":"400",
                                        "feilmelding":"BAD_REQUEST"
                                    },
                                    "sakInformasjonListe":[]
                                }
                            """.trimIndent()
                        )
                )
                .willSetStateTo("ok")
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                {
                                    "feil":null, 
                                    "sakInformasjonListe":[
                                        {
                                            "sakId":"1",
                                            "sakType":"OMSORG",
                                            "sakStatus":"OPPRETTET",
                                            "saksbehandlendeEnhetId":"4100",
                                            "nyopprettet":true,
                                            "tilknyttedeSaker":[]
                                        }
                                    ]
                                }
                            """.trimIndent()
                        )
                )
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(OPPGAVE_PATH))
                .willReturn(
                    WireMock.created()
                        .withBody("""{"id":123}""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                )
        )

        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        val melding = repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                        omsorgsyter = "12345678910",
                        saker = listOf(
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "12345678910",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde(""),
                        innlesingId = InnlesingId.generate(),
                        correlationId = CorrelationId.generate(),
                    )
                ),
            )

        handler.process()

        oppgaveRepo.findForMelding(melding.id!!).single().also {
            assertInstanceOf(Oppgave.Status.Klar::class.java, it.status)
        }
        assertThrows<BestemSakClientException> {
            oppgaveService.process()
        }
        oppgaveRepo.findForMelding(melding.id!!).single().also { oppgave ->
            assertInstanceOf(Oppgave.Status.Retry::class.java, oppgave.status).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertContains(it.melding, "Feil ved kall til http://localhost:9991/pen/api/bestemsak/v1")
                assertContains(it.melding, "BAD_REQUEST")
            }
        }
        oppgaveService.process()!!.also { oppgave ->
            assertInstanceOf(Oppgave.Status.Ferdig::class.java, oppgave.status).also {
                assertEquals("123", it.oppgaveId)
            }
        }
    }

    @Test
    fun `gitt at prosesseringen ender med retry, havner den aktuelle raden i karantene før den forsøkes på nytt igjen`() {
        wiremock.stubForPdlTransformer()
        given(clock.instant()).willReturn(
            Clock.systemUTC().instant(), //karantene -- handler kalles
            Clock.systemUTC().instant(), //karantene
            Clock.systemUTC().instant().plus(2, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(4, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(6, ChronoUnit.HOURS), //karantenetid utløpt
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                {
                                    "feil":{
                                        "feilKode":"400",
                                        "feilmelding":"BAD_REQUEST"
                                    },
                                    "sakInformasjonListe":[]
                                }
                            """.trimIndent()
                        )
                )
                .willSetStateTo("ok")
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                {
                                    "feil":null, 
                                    "sakInformasjonListe":[
                                        {
                                            "sakId":"1",
                                            "sakType":"OMSORG",
                                            "sakStatus":"OPPRETTET",
                                            "saksbehandlendeEnhetId":"4100",
                                            "nyopprettet":true,
                                            "tilknyttedeSaker":[]
                                        }
                                    ]
                                }
                            """.trimIndent()
                        )
                )
        )
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(OPPGAVE_PATH))
                .willReturn(
                    WireMock.created()
                        .withBody("""{"id":123}""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                )
        )

        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        val melding = repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                        omsorgsyter = "12345678910",
                        saker = listOf(
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "12345678910",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde(""),
                        innlesingId = InnlesingId.generate(),
                        correlationId = CorrelationId.generate(),
                    )
                ),
            )

        handler.process()

        assertInstanceOf(Oppgave.Status.Klar::class.java, oppgaveRepo.findForMelding(melding.id!!).single().status)

        assertThrows<BestemSakClientException> {
            oppgaveService.process()
        }

        assertInstanceOf(
            Oppgave.Status.Retry::class.java,
            oppgaveRepo.findForMelding(melding.id!!).single().status
        ).also {
            assertEquals(1, it.antallForsøk)
        }

        assertNull(oppgaveService.process())
        assertNull(oppgaveService.process())
        assertNotNull(oppgaveService.process())

        assertInstanceOf(Oppgave::class.java, oppgaveRepo.findForMelding(melding.id!!).single())
            .also {
                assertInstanceOf(Oppgave.Status.Ferdig::class.java, it.status).also {
                    assertEquals("123", it.oppgaveId)
                }
            }
    }

    @Test
    fun `gitt at en oppgave har blitt forsøkt opprettet maks antall ganger uten hell får den status feilet`() {
        wiremock.stubForPdlTransformer()
        /**
         * Stiller klokka litt fram i tid for å unngå at [Oppgave.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(BESTEM_SAK_PATH))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                {
                                    "feil":{
                                        "feilKode":"400",
                                        "feilmelding":"BAD_REQUEST"
                                    },
                                    "sakInformasjonListe":[]
                                }
                            """.trimIndent()
                        )
                )
        )

        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        val melding = repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                        omsorgsyter = "12345678910",
                        saker = listOf(
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "12345678910",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.DELT_BARNETRYGD,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde(""),
                        innlesingId = InnlesingId.generate(),
                        correlationId = CorrelationId.generate(),
                    )
                ),
            )

        handler.process()

        oppgaveRepo.findForMelding(melding.id!!).single().also {
            assertInstanceOf(Oppgave.Status.Klar::class.java, it.status)
        }
        assertThrows<BestemSakClientException> {
            oppgaveService.process()
        }

        oppgaveRepo.findForMelding(melding.id!!).single().also { oppgave ->
            assertInstanceOf(Oppgave.Status.Retry::class.java, oppgave.status).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertContains(it.melding, "Feil ved kall til http://localhost:9991/pen/api/bestemsak/v1")
                assertContains(it.melding, "BAD_REQUEST")
            }
        }

        assertThrows<BestemSakClientException> {
            oppgaveService.process()
        }
        assertThrows<BestemSakClientException> {
            oppgaveService.process()
        }
        assertThrows<BestemSakClientException> {
            oppgaveService.process()
        }

        oppgaveRepo.findForMelding(melding.id!!).single().also { oppgave ->
            oppgave.statushistorikk
                .also { status ->
                    assertEquals(1, status.count { it is Oppgave.Status.Klar })
                    assertEquals(3, status.count { it is Oppgave.Status.Retry })
                    assertEquals(1, status.count { it is Oppgave.Status.Feilet })
                    assertEquals(0, status.count { it is Oppgave.Status.Ferdig })
                }
            assertInstanceOf(Oppgave.Status.Feilet::class.java, oppgave.status)
        }
    }
}
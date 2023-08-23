package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlException
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


class MeldingsprosesseringTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMeldingService

    @MockBean
    private lateinit var clock: Clock

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var oppgaveService: OppgaveService


    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `gitt at en rad feiler, så skal den kunne retryes og gå bra på et senere tidspunkt`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error_bad_request.json")
                )
                .willSetStateTo("not found")
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("not found")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error_not_found.json")
                )
                .willSetStateTo("ok")
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                )
        )
        /**
         * Stiller klokka litt fram i tid for å unngå at [OmsorgsarbeidMelding.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

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
                                        fom = YearMonth.of(2018, Month.SEPTEMBER),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = UUID.randomUUID().toString(),
            )
        )

        assertInstanceOf(OmsorgsarbeidMelding.Status.Klar::class.java, repo.find(melding.id!!).status)

        assertThrows<PdlException> {
            handler.process()
        }

        repo.find(melding.id!!).let { m ->
            assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, m.status).let {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
            }
        }
        assertEquals(emptyList<FullførtBehandling>(), behandlingRepo.finnForOmsorgsyter("12345678910"))

        assertThrows<PdlException> {
            handler.process()
        }

        repo.find(melding.id!!).let { m ->
            assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, m.status).let {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
            }
        }
        assertEquals(emptyList<FullførtBehandling>(), behandlingRepo.finnForOmsorgsyter("12345678910"))


        handler.process().also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, it.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(BehandlingUtfall.Innvilget::class.java, it.utfall)
            }
        }
        assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())
    }

    @Test
    fun `gitt at prosessering ender med retry, havner den aktuelle raden i karantene før den forsøkes på nytt igjen`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error_bad_request.json")
                )
                .willSetStateTo("ok")
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                )
        )
        given(clock.instant()).willReturn(
            Clock.systemUTC().instant(), //karantene
            Clock.systemUTC().instant().plus(2, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(4, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(6, ChronoUnit.HOURS), //karantenetid utløpt
        )
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

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
                                        fom = YearMonth.of(2018, Month.SEPTEMBER),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = UUID.randomUUID().toString(),
            )
        )

        assertInstanceOf(OmsorgsarbeidMelding.Status.Klar::class.java, repo.find(melding.id!!).status)

        assertThrows<PdlException> {
            handler.process()
        }
        assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, repo.find(melding.id!!).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertEquals(emptyList<FullførtBehandling>(), handler.process())
        assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, repo.find(melding.id!!).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertEquals(emptyList<FullførtBehandling>(), handler.process())
        assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, repo.find(melding.id!!).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertEquals(1, handler.process().count())

        assertInstanceOf(OmsorgsarbeidMelding.Status.Ferdig::class.java, repo.find(melding.id!!).status)
    }

    @Test
    fun `gitt at en melding har blitt prosessert på nytt uten hell maks antall ganger skal det opprettes en oppgave`() {
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error_bad_request.json")
                )
                .willSetStateTo("not found")
        )
        wiremock.stubFor(
            WireMock.post(WireMock.urlEqualTo(PDL_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("not found")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error_not_found.json")
                )
                .willSetStateTo("not found")
        )
        /**
         * Stiller klokka litt fram i tid for å unngå at [OmsorgsarbeidMelding.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
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
                                        fom = YearMonth.of(2018, Month.SEPTEMBER),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "07081812345"
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

        assertInstanceOf(OmsorgsarbeidMelding.Status.Klar::class.java, repo.find(melding.id!!).status)

        assertThrows<PdlException> {
            handler.process()
        }
        assertThrows<PdlException> {
            handler.process()
        }
        assertThrows<PdlException> {
            handler.process()
        }
        assertThrows<PdlException> {
            handler.process()
        }

        assertEquals(emptyList<FullførtBehandling>(), handler.process())

        repo.find(melding.id!!).also { m ->
            assertInstanceOf(OmsorgsarbeidMelding.Status.Klar::class.java, m.statushistorikk[0])
            assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, m.statushistorikk[1]).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertEquals(
                    "no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlException: Ugyldig ident",
                    it.melding
                )
            }
            assertInstanceOf(OmsorgsarbeidMelding.Status.Retry::class.java, m.statushistorikk[2]).also {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertEquals(
                    "no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlException: Fant ikke person",
                    it.melding
                )
            }
            assertInstanceOf(OmsorgsarbeidMelding.Status.Feilet::class.java, m.status)
            assertInstanceOf(OmsorgsarbeidMelding.Status.Feilet::class.java, m.statushistorikk.last())
        }

        assertEquals(0, behandlingRepo.finnForOmsorgsyter("12345678910").count())

        oppgaveRepo.findForMelding(melding.id!!).single().also { oppgave ->
            assertInstanceOf(OppgaveDetaljer.UspesifisertFeilsituasjon::class.java, oppgave.detaljer).also {
                assertEquals("12345678910", it.omsorgsyter)
            }
            assertEquals(null, oppgave.behandlingId)
            assertEquals(melding.id, oppgave.meldingId)
            assertEquals(correlationId, oppgave.correlationId.toString())
            assertInstanceOf(Oppgave.Status.Klar::class.java, oppgave.status)
        }
    }
}
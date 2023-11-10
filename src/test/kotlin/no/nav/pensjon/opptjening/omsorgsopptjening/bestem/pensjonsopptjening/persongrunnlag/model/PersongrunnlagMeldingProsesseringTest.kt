package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.PersonOppslagException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
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
import kotlin.test.assertContains
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka


class PersongrunnlagMeldingProsesseringTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

    @MockBean
    private lateinit var clock: Clock

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo


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
         * Stiller klokka litt fram i tid for å unngå at [PersongrunnlagMelding.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)

        val melding = repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.SEPTEMBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
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

        assertInstanceOf(PersongrunnlagMelding.Status.Klar::class.java, repo.find(melding.id).status)

        assertThrows<PersonOppslagException> {
            handler.process()
        }

        repo.find(melding.id).let { m ->
            assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, m.status).let {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
            }
        }
        assertEquals(emptyList<FullførtBehandling>(), behandlingRepo.finnForOmsorgsyter("12345678910"))

        assertThrows<PersonOppslagException> {
            handler.process()
        }

        repo.find(melding.id).let { m ->
            assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, m.status).let {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
            }
        }
        assertEquals(emptyList<FullførtBehandling>(), behandlingRepo.finnForOmsorgsyter("12345678910"))


        handler.process()!!.also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(BehandlingUtfall.Innvilget::class.java, it.utfall)
                assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())
                assertEquals(1, godskrivOpptjeningRepo.findForBehandling(it.id).count())
            }
        }
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
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)

        val melding = repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.SEPTEMBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
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

        assertInstanceOf(PersongrunnlagMelding.Status.Klar::class.java, repo.find(melding.id).status)

        assertThrows<PersonOppslagException> {
            handler.process()
        }
        assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, repo.find(melding.id).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertEquals(null, handler.process())

        assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, repo.find(melding.id).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertEquals(null, handler.process())

        assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, repo.find(melding.id).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertEquals(1, handler.process()!!.antallBehandlinger(2020))

        assertInstanceOf(PersongrunnlagMelding.Status.Ferdig::class.java, repo.find(melding.id).status)
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
         * Stiller klokka litt fram i tid for å unngå at [PersongrunnlagMelding.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)

        val melding = repo.persist(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.SEPTEMBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
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

        assertInstanceOf(PersongrunnlagMelding.Status.Klar::class.java, repo.find(melding.id).status)

        assertThrows<PersonOppslagException> {
            handler.process()
        }
        assertThrows<PersonOppslagException> {
            handler.process()
        }
        assertThrows<PersonOppslagException> {
            handler.process()
        }
        assertThrows<PersonOppslagException> {
            handler.process()
        }

        assertEquals(null, handler.process())

        repo.find(melding.id).also { m ->
            assertInstanceOf(PersongrunnlagMelding.Status.Klar::class.java, m.statushistorikk[0])
            assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, m.statushistorikk[1]).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertContains( //wrapper
                    it.melding,
                    "PersonOppslagException(msg=Feil ved henting av person, throwable=no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlException: Ugyldig ident)"
                )
                assertContains( //rotårsak
                    it.melding,
                    "Caused by: no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlException: Ugyldig ident"
                )
            }
            assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, m.statushistorikk[2]).also {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertContains( //wrapper
                    it.melding,
                    "PersonOppslagException(msg=Feil ved henting av person, throwable=no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlException: Fant ikke person)"
                )
                assertContains( //rotårsak
                    it.melding,
                    "Caused by: no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.external.pdl.PdlException: Fant ikke person"
                )
            }
            assertInstanceOf(PersongrunnlagMelding.Status.Feilet::class.java, m.status)
            assertInstanceOf(PersongrunnlagMelding.Status.Feilet::class.java, m.statushistorikk.last())
        }

        assertEquals(0, behandlingRepo.finnForOmsorgsyter("12345678910").count())

        oppgaveRepo.findForMelding(melding.id).single().also { oppgave ->
            assertEquals(
                OppgaveDetaljer.MottakerOgTekst(
                    oppgavemottaker = "12345678910",
                    oppgavetekst = """Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch."""
                ),
                oppgave.detaljer
            )
            assertEquals(null, oppgave.behandlingId)
            assertEquals(melding.id, oppgave.meldingId)
            assertEquals(melding.correlationId, oppgave.correlationId)
            assertEquals(melding.innlesingId, oppgave.innlesingId)
            assertInstanceOf(Oppgave.Status.Klar::class.java, oppgave.status)
        }
    }
}
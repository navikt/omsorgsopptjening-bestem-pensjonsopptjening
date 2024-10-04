package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.medlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
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
        wiremock.medlemIFolketrygden()

        val melding = repo.lagre(
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

        assertInstanceOf(PersongrunnlagMelding.Status.Klar::class.java, repo.find(melding!!).status)

        handler.process()

        repo.find(melding).let { m ->
            assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, m.status).let {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
            }
        }
        assertEquals(emptyList<FullførtBehandling>(), behandlingRepo.finnForOmsorgsyter("12345678910"))

        handler.process()

        repo.find(melding).let { m ->
            assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, m.status).let {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
            }
        }
//        assertEquals(emptyList<FullførtBehandling>(),
        assertThat(behandlingRepo.finnForOmsorgsyter("12345678910")).isEmpty()

        handler.process()!!.first().also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainOmsorgskategori.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(BehandlingUtfall.Innvilget::class.java, it.utfall)
                assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())
                assertEquals(1, godskrivOpptjeningRepo.findForBehandling(it.id).count())
            }
        }
    }

    // TOOD: JKR: denne testen kan ikke kjøres isolert
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
            // TODO: Må finne en annen løsning på dette
            Clock.systemUTC().instant(), //karantene
            Clock.systemUTC().instant().plus(1, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(2, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(3, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(4, ChronoUnit.HOURS), //karantene
            Clock.systemUTC().instant().plus(10, ChronoUnit.HOURS), //karantenetid utløpt
        )
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.medlemIFolketrygden()

        val melding = repo.lagre(
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

        assertThat(repo.find(melding!!).status)
            .isInstanceOf(PersongrunnlagMelding.Status.Klar::class.java)

        handler.process()

        assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, repo.find(melding).status).also {
            assertEquals(1, it.antallForsøk)
        }

        assertThat(handler.process()).isEmpty()

        assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, repo.find(melding).status).also {
            assertThat(it.antallForsøk).isOne()
        }

        assertThat(handler.process()).isEmpty()

        assertInstanceOf(PersongrunnlagMelding.Status.Retry::class.java, repo.find(melding).status).also {
            assertThat(it.antallForsøk).isEqualTo(1)
        }

        assertThat(handler.process()!!.first().antallBehandlinger()).isEqualTo(1)

        assertInstanceOf(PersongrunnlagMelding.Status.Ferdig::class.java, repo.find(melding).status)
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

        val innlesingId = InnlesingId.generate()
        val correlationId = CorrelationId.generate()

        val melding = repo.lagre(
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
                    innlesingId = innlesingId,
                    correlationId = correlationId,
                )
            ),
        )

        assertInstanceOf(PersongrunnlagMelding.Status.Klar::class.java, repo.find(melding!!).status)

        handler.process()
        handler.process()
        handler.process()
        handler.process()

        assertThat(handler.process()).isEmpty()

        repo.find(melding).also { m ->
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

        oppgaveRepo.findForMelding(melding).single().also { oppgave ->
            assertEquals(
                OppgaveDetaljer.MottakerOgTekst(
                    oppgavemottaker = "12345678910",
                    oppgavetekst = setOf("""Godskriving omsorgspoeng: Manuell behandling. Godskrivingen kunne ikke behandles av batch.""")
                ),
                oppgave.detaljer
            )
            assertEquals(null, oppgave.behandlingId)
            assertEquals(melding, oppgave.meldingId)
            assertEquals(correlationId, oppgave.correlationId)
            assertEquals(innlesingId, oppgave.innlesingId)
            assertInstanceOf(Oppgave.Status.Klar::class.java, oppgave.status)
        }
    }
}
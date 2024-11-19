package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import java.time.temporal.ChronoUnit
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka


class OppgaveServiceProsesseringTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingProcessingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

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
        clock.nesteTikk(clock.nåtid().plus(10, ChronoUnit.DAYS))
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

        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
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

        handler.process()

        oppgaveRepo.findForMelding(melding!!).single().also {
            assertInstanceOf(Oppgave.Status.Klar::class.java, it.status)
        }

        oppgaveService.process()

        oppgaveRepo.findForMelding(melding).single().also { oppgave ->
            assertInstanceOf(Oppgave.Status.Retry::class.java, oppgave.status).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertThat(it.melding).contains("Feil ved kall til http://localhost:9991/pen/api/bestemsak/v1")
                assertThat(it.melding).contains("BAD_REQUEST")
            }
        }
        oppgaveService.processAndExpectResult().first().also { oppgave ->
            assertInstanceOf(Oppgave.Status.Ferdig::class.java, oppgave.status).also {
                assertEquals("123", it.oppgaveId)
            }
        }
    }

    @Test
    fun `gitt at prosesseringen ender med retry, havner den aktuelle raden i karantene før den forsøkes på nytt igjen`() {
        wiremock.stubForPdlTransformer()
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

        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
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

        handler.process()

        assertInstanceOf(Oppgave.Status.Klar::class.java, oppgaveRepo.findForMelding(melding!!).single().status)

        oppgaveService.process()

        assertInstanceOf(
            Oppgave.Status.Retry::class.java,
            oppgaveRepo.findForMelding(melding).single().status
        ).also {
            assertThat(it.antallForsøk).isEqualTo(1)
        }

        clock.nesteTikk(clock.nåtid().plus(2, ChronoUnit.HOURS)) //karantene
        assertThat(oppgaveService.process()).isInstanceOf(Resultat.FantIngenDataÅProsessere::class.java)
        clock.nesteTikk(clock.nåtid().plus(2, ChronoUnit.HOURS)) //karantene
        assertThat(oppgaveService.process()).isInstanceOf(Resultat.FantIngenDataÅProsessere::class.java)
        clock.nesteTikk(clock.nåtid().plus(2, ChronoUnit.HOURS)) //karantenetid utløpt
        assertThat(oppgaveService.processAndExpectResult()).isNotEmpty()

        assertInstanceOf(Oppgave::class.java, oppgaveRepo.findForMelding(melding).single())
            .also { oppgave ->
                assertInstanceOf(Oppgave.Status.Ferdig::class.java, oppgave.status).also { ferdig ->
                    assertThat(ferdig.oppgaveId).isEqualTo("123")
                }
            }
    }

    @Test
    fun `gitt at en oppgave har blitt forsøkt opprettet maks antall ganger uten hell får den status feilet`() {
        wiremock.stubForPdlTransformer()
        /**
         * Stiller klokka litt fram i tid for å unngå at [Oppgave.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        clock.nesteTikk(clock.nåtid().plus(10, ChronoUnit.DAYS))
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

        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()

        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "07081812345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = januar(2020),
                                    tom = desember(2020),
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

        handler.process()

        oppgaveRepo.findForMelding(melding!!).single().also {
            assertInstanceOf(Oppgave.Status.Klar::class.java, it.status)
        }
        oppgaveService.process()

        oppgaveRepo.findForMelding(melding).single().also { oppgave ->
            assertInstanceOf(Oppgave.Status.Retry::class.java, oppgave.status).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertThat(it.melding).contains("Feil ved kall til http://localhost:9991/pen/api/bestemsak/v1")
                assertThat(it.melding).contains("BAD_REQUEST")
            }
        }

        oppgaveService.process()
        oppgaveService.process()
        oppgaveService.process()

        oppgaveRepo.findForMelding(melding).single().also { oppgave ->
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

fun OppgaveService.processAndExpectResult(): List<Oppgave.Persistent> {
    return when (val result = this.process()) {
        is Resultat.FantIngenDataÅProsessere -> fail("Expecting result")
        is Resultat.Prosessert -> result.data
    }
}
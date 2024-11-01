package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.Resultat
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.TestKlokke
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.external.PENBrevClient.Companion.createPath
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.bestemSakOk
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførteBehandlinger
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.processAndExpectResult
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka


class BrevProsesseringTest(
    @Value("\${PEN_BASE_URL}")
    private val baseUrl: String,
) : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingService: PersongrunnlagMeldingProcessingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var brevService: BrevService

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `gitt at en rad feiler, så skal den kunne retryes og gå bra på et senere tidspunkt`() {
        wiremock.stubForPdlTransformer()
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.ingenPensjonspoeng("04010012797") //far
        wiremock.bestemSakOk()
        wiremock.ingenUnntaksperioderForMedlemskap()

        val sendBrevPath = URI(createPath(baseUrl, "12345")).toURL().path

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(sendBrevPath))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.forbidden()
                )
                .willSetStateTo("feil 2")
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(sendBrevPath))
                .inScenario("retry")
                .whenScenarioStateIs("feil 2")
                .willReturn(
                    WireMock.notFound()
                )
                .willSetStateTo("ok")
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(sendBrevPath))
                .inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                {
                                    "journalpostId": "acoc2323o"
                                }
                            """.trimIndent()
                        )
                )
        )

        /**
         * Stiller klokka litt fram i tid for å unngå at [Brev.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        clock.nesteTikk(clock.nåtid().plus(10, ChronoUnit.DAYS))
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)

        val (behandling, brev) = persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        ).let {
            persongrunnlagMeldingService.processAndExpectResult().first().single().let { behandling ->
                Assertions.assertTrue(behandling.erInnvilget())
                behandling to brevRepository.findForBehandling(behandling.id).singleOrNull()!!
            }
        }

        assertInstanceOf(Brev.Status.Klar::class.java, brevRepository.find(brev.id).status)

        brevService.process()

        brevRepository.find(brev.id).let { m ->
            assertInstanceOf(Brev.Status.Retry::class.java, m.status).let {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertThat(it.melding).contains("Forbidden")
            }
        }

        brevService.process()

        brevRepository.find(brev.id).let { m ->
            assertInstanceOf(Brev.Status.Retry::class.java, m.status).let {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertThat(it.melding).contains("Feil fra brevtjenesten: vedtak finnes ikke")
            }
        }

        brevService.processAndExpectResult().first().also { b ->
            assertEquals(2020, b.omsorgsår)
            assertEquals("12345678910", b.omsorgsyter)
            assertEquals(behandling.id, b.behandlingId)
            assertEquals(behandling.meldingId, b.meldingId)
            assertInstanceOf(Brev.Status.Ferdig::class.java, b.status).also {
                assertEquals("acoc2323o", it.journalpost)
            }
            assertEquals(1, brevRepository.findForBehandling(behandling.id).count())
        }
    }

    @Test
    fun `gitt at en melding har blitt prosessert på nytt uten hell maks antall ganger skal det opprettes en oppgave`() {
        val sendBrevPath = URI(createPath(baseUrl, "42")).toURL().path

        wiremock.stubForPdlTransformer()
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.ingenPensjonspoeng("04010012797") //far
        wiremock.bestemSakOk()
        wiremock.ingenUnntaksperioderForMedlemskap()

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(sendBrevPath))
                .willReturn(
                    WireMock.forbidden()
                )
        )

        /**
         * Stiller klokka litt fram i tid for å unngå at [Brev.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        clock.nesteTikk(clock.nåtid().plus(10, ChronoUnit.DAYS))
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)

        val (behandling, brev) = persongrunnlagRepo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = PersongrunnlagMeldingKafka(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        PersongrunnlagMeldingKafka.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = listOf(
                                PersongrunnlagMeldingKafka.Hjelpestønadperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
                                )
                            )
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        ).let {
            persongrunnlagMeldingService.processAndExpectResult().first().single().let { behandling ->
                Assertions.assertTrue(behandling.erInnvilget())
                behandling to brevRepository.findForBehandling(behandling.id).singleOrNull()!!
            }
        }

        assertInstanceOf(Brev.Status.Klar::class.java, brevRepository.find(brev.id).status)

        brevService.process()
        brevService.process()
        brevService.process()
        brevService.process()

        brevRepository.find(brev.id).also { b ->
            assertEquals(2020, b.omsorgsår)
            assertEquals("12345678910", b.omsorgsyter)
            assertEquals(behandling.id, b.behandlingId)
            assertEquals(behandling.meldingId, b.meldingId)
            assertInstanceOf(Brev.Status.Feilet::class.java, b.status)
            assertEquals(1, brevRepository.findForBehandling(behandling.id).count())
        }
    }
}

private fun BrevService.processAndExpectResult(): List<Brev.Persistent> {
    return when(val result = this.process()){
        is Resultat.FantIngenDataÅProsessere -> fail("Expecting result")
        is Resultat.Prosessert -> result.data
    }
}
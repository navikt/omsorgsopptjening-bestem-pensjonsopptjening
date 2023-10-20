package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.Brev
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevClientException
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.model.BrevService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.brev.repository.BrevRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.bestemSakOk
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenPensjonspoeng
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.test.assertContains
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka


class BrevProsesseringTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @Autowired
    private lateinit var persongrunnlagMeldingService: PersongrunnlagMeldingService

    @MockBean
    private lateinit var clock: Clock

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var brevRepository: BrevRepository

    @Autowired
    private lateinit var brevService: BrevService

    @Autowired
    private lateinit var brevClient: BrevClient


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

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(SEND_BREV_PATH))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                    WireMock.forbidden()
                )
                .willSetStateTo("feil 2")
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(SEND_BREV_PATH))
                .inScenario("retry")
                .whenScenarioStateIs("feil 2")
                .willReturn(
                    WireMock.notFound()
                )
                .willSetStateTo("ok")
        )

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(SEND_BREV_PATH))
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
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        given(gyldigOpptjeningår.get()).willReturn(listOf(2020))

        val (behandling, brev) = persongrunnlagRepo.persist(
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
                                    medlemskap = MedlemIFolketrygden.Ukjent,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
medlemskap = MedlemIFolketrygden.Ukjent,
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        ).let {
            persongrunnlagMeldingService.process().single().let { behandling ->
                Assertions.assertTrue(behandling.erInnvilget())
                behandling to brevRepository.findForBehandling(behandling.id).singleOrNull()!!
            }
        }

        assertInstanceOf(Brev.Status.Klar::class.java, brevRepository.find(brev.id).status)

        assertThrows<BrevClientException> {
            brevService.process()
        }

        brevRepository.find(brev.id).let { m ->
            assertInstanceOf(Brev.Status.Retry::class.java, m.status).let {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertContains(it.melding, "Forbidden")
            }
        }

        assertThrows<BrevClientException> {
            brevService.process()
        }

        brevRepository.find(brev.id).let { m ->
            assertInstanceOf(Brev.Status.Retry::class.java, m.status).let {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertContains(it.melding, "Not Found")
            }
        }

        brevService.process()!!.also { b ->
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
        wiremock.stubForPdlTransformer()
        wiremock.ingenPensjonspoeng("12345678910") //mor
        wiremock.ingenPensjonspoeng("04010012797") //far
        wiremock.bestemSakOk()

        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(SEND_BREV_PATH))
                .willReturn(
                    WireMock.forbidden()
                )
        )

        /**
         * Stiller klokka litt fram i tid for å unngå at [Brev.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))
        given(gyldigOpptjeningår.get()).willReturn(listOf(2020))

        val (behandling, brev) = persongrunnlagRepo.persist(
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
medlemskap = MedlemIFolketrygden.Ukjent,
                                ),
                                PersongrunnlagMeldingKafka.Omsorgsperiode(
                                    fom = YearMonth.of(2018, Month.JANUARY),
                                    tom = YearMonth.of(2030, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.HJELPESTØNAD_FORHØYET_SATS_3,
                                    omsorgsmottaker = "03041212345",
                                    kilde = Kilde.BARNETRYGD,
medlemskap = MedlemIFolketrygden.Ukjent,
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        ).let {
            persongrunnlagMeldingService.process().single().let { behandling ->
                Assertions.assertTrue(behandling.erInnvilget())
                behandling to brevRepository.findForBehandling(behandling.id).singleOrNull()!!
            }
        }

        assertInstanceOf(Brev.Status.Klar::class.java, brevRepository.find(brev.id).status)

        assertThrows<BrevClientException> {
            brevService.process()
        }
        assertThrows<BrevClientException> {
            brevService.process()
        }
        assertThrows<BrevClientException> {
            brevService.process()
        }
        assertThrows<BrevClientException> {
            brevService.process()
        }

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
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID

@ContextConfiguration(classes = [GyldigOpptjeningsår2020::class])
class RetryTilFailureTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMessageService

    @MockBean
    private lateinit var clock: Clock

    companion object {
        @RegisterExtension
        private val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun test() {
        wiremock.stubPdl(
            listOf(
                PdlScenario(body = "error_bad_request.json", setState = "feil2"),
                PdlScenario(inState = "feil2", body = "error_not_found.json", setState = "feil3"),
                PdlScenario(inState = "feil3", body = "error_not_found.json", setState = "feil4"),
                PdlScenario(inState = "feil4", body = "error_not_found.json"),
            )
        )
        /**
         * Stiller klokka litt fram i tid for å unngå at [PersistertKafkaMelding.Status.Retry.karanteneTil] fører til at vi hopper over raden.
         */
        given(clock.instant()).willReturn(Instant.now().plus(10, ChronoUnit.DAYS))

        val melding = repo.persist(
            PersistertKafkaMelding(
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

        assertInstanceOf(PersistertKafkaMelding.Status.Klar::class.java, repo.find(melding.id!!).status)

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
            assertInstanceOf(PersistertKafkaMelding.Status.Klar::class.java, m.statushistorikk[0])
            assertInstanceOf(PersistertKafkaMelding.Status.Retry::class.java, m.statushistorikk[1]).also {
                assertEquals(1, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertEquals(
                    "no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlException: Ugyldig ident",
                    it.melding
                )
            }
            assertInstanceOf(PersistertKafkaMelding.Status.Retry::class.java, m.statushistorikk[2]).also {
                assertEquals(2, it.antallForsøk)
                assertEquals(3, it.maxAntallForsøk)
                assertEquals(it.tidspunkt.plus(5, ChronoUnit.HOURS), it.karanteneTil)
                assertEquals(
                    "no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlException: Fant ikke person",
                    it.melding
                )
            }
            assertInstanceOf(PersistertKafkaMelding.Status.Feilet::class.java, m.status)
            assertInstanceOf(PersistertKafkaMelding.Status.Feilet::class.java, m.statushistorikk.last())
        }

        assertEquals(0, behandlingRepo.finnForOmsorgsyter("12345678910").count())
    }
}
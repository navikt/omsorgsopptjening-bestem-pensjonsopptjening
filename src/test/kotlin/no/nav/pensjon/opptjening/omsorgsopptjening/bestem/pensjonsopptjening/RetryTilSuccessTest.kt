package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
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
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Clock
import java.time.Instant
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID


class RetryTilSuccessTest : SpringContextTest.NoKafka() {

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
                PdlScenario(inState = "feil2", body = "error_not_found.json", setState = "success"),
                PdlScenario(inState = "success", body = "fnr_1bruk.json", setState = "hent barn 1"),
                PdlScenario(inState = "hent barn 1", body = "fnr_barn_2ar_2020.json"),
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
                assertInstanceOf(AutomatiskGodskrivingUtfall.Innvilget::class.java, it.utfall)
            }
        }
        assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())
    }
}
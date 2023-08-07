package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErFylt17VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erAvslått
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.serialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvslagMangeOmsorgsmottakereHarLikeMangeMånederOmsorgTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMessageService

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

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
                PdlScenario(body = "fnr_1bruk.json", setState = "hent barn 1"),
                PdlScenario(inState = "hent barn 1", body = "fnr_barn_2ar_2020.json", setState = "hent annen forelder"),
                PdlScenario(
                    inState = "hent annen forelder",
                    body = "fnr_samme_fnr_gjeldende_og_historisk.json",
                    setState = "hent annen forelder 2"
                ),
                PdlScenario(
                    inState = "hent annen forelder 2",
                    body = "fnr_1bruk_pluss_historisk.json",
                ),
            )
        )
        willAnswer {
            listOf(2020, 2021)
        }.given(gyldigOpptjeningår).get()

        val correlationId = UUID.randomUUID()

        repo.persist(
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
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.JULY),
                                        prosent = 100,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JUNE),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "01018212345",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.JULY),
                                        prosent = 50,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = correlationId.toString(),
            )
        )

        val behandling = handler.process().let { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, it.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.Avslag::class.java, it.utfall)

                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erAvslått<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>() }

                it.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                    assertFalse(it.grunnlag.omsorgsyterHarFlest())
                    assertTrue(it.grunnlag.flereHarLikeMange())
                    assertEquals(
                        mapOf(
                            "12345678910" to 7,
                            "04010012797" to 7,
                            "01018212345" to 7,
                        ), it.grunnlag.yterTilAntall
                    )
                }
            }
        }
        oppgaveRepo.findForMelding(behandling.kafkaMeldingId)!!.also { oppgave ->
            assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java, oppgave.detaljer).also {
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals("04010012797", it.annenOmsorgsyter)
                assertEquals("""
                    Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 04010012797 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.
                """.trimIndent(), it.oppgaveTekst)
            }
            assertEquals(behandling.id, oppgave.behandlingId)
            assertEquals(behandling.kafkaMeldingId, oppgave.meldingId)
            assertEquals(correlationId, oppgave.correlationId)
            assertInstanceOf(Oppgave.Status.Klar::class.java, oppgave.status)
        }

        oppgaveService.process()!!.also { oppgave ->
            assertInstanceOf(Oppgave.Status.Ferdig::class.java, oppgave.status).also {
                assertEquals("oppgaveId", it.oppgaveId)
            }
        }
    }
}
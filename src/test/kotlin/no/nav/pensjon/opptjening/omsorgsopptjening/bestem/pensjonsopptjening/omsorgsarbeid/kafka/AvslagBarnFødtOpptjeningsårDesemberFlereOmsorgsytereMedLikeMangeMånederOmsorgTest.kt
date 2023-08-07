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
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveService
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


class AvslagBarnFødtOpptjeningsårDesemberFlereOmsorgsytereMedLikeMangeMånederOmsorgTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMessageService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveService: OppgaveService

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

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
                PdlScenario(inState = "hent barn 1", body = "fnr_barn_0ar_des_2020.json", setState = "hent forelder 2"),
                PdlScenario(
                    inState = "hent forelder 2",
                    body = "fnr_samme_fnr_gjeldende_og_historisk.json",
                    setState = "hent barn 2"
                ),
                PdlScenario(inState = "hent barn 2", body = "fnr_barn_0ar_des_2020.json"),
            )
        )
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        val correlationId = UUID.randomUUID().toString()

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
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.JUNE),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JULY),
                                        tom = YearMonth.of(2021, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    ),
                                )
                            ),
                        ),
                        rådata = RådataFraKilde("")
                    )
                ),
                correlationId = correlationId,
            )
        )

        val behandling = handler.process().let { result ->
            result.single().also { behandling ->
                assertEquals(2020, behandling.omsorgsAr)
                assertEquals("12345678910", behandling.omsorgsyter)
                assertEquals("01122012345", behandling.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, behandling.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, behandling.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.Avslag::class.java, behandling.utfall)

                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erAvslått<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>() }

                behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                    assertFalse(it.grunnlag.omsorgsyterHarFlest())
                    assertTrue(it.grunnlag.flereHarLikeMange())
                    assertEquals(
                        mapOf(
                            "12345678910" to 6,
                            "04010012797" to 6,
                        ), it.grunnlag.yterTilAntall
                    )
                }
            }
        }

        oppgaveRepo.findForMelding(behandling.kafkaMeldingId)!!.also { oppgave ->
            assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår::class.java, oppgave.detaljer).also {
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("01122012345", it.omsorgsmottaker)
                assertEquals(
                    """
                    Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01122012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene.
                """.trimIndent(), it.oppgaveTekst
                )
            }
            assertEquals(behandling.id, oppgave.behandlingId)
            assertEquals(behandling.kafkaMeldingId, oppgave.meldingId)
            assertEquals(correlationId, oppgave.correlationId.toString())
            assertInstanceOf(Oppgave.Status.Klar::class.java, oppgave.status)
        }

        oppgaveService.process()!!.also { oppgave ->
            assertInstanceOf(Oppgave.Status.Ferdig::class.java, oppgave.status).also {
                assertEquals("oppgaveId", it.oppgaveId)
            }
        }
    }
}
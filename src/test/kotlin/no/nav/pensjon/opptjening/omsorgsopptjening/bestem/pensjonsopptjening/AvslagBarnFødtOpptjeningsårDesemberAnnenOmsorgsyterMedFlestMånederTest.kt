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
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.OppgaveRepo
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
import kotlin.test.assertNull
import kotlin.test.assertTrue


class AvslagBarnFødtOpptjeningsårDesemberAnnenOmsorgsyterMedFlestMånederTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

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
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.FEBRUARY),
                                        prosent = 50,
                                        omsorgsmottaker = "01122012345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.FEBRUARY),
                                        prosent = 50,
                                        omsorgsmottaker = "01122012345"
                                    ),
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.MARCH),
                                        tom = YearMonth.of(2021, Month.MAY),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
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

        handler.process().also { result ->
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
                    assertFalse(it.grunnlag.flereHarLikeMange())
                    assertEquals(
                        mapOf(
                            "12345678910" to 2,
                            "04010012797" to 5,
                        ), it.grunnlag.yterTilAntall
                    )
                }
            }
        }
        assertNull(oppgaveRepo.findForMelding(melding.id!!))
    }
}
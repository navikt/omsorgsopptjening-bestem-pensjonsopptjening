package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErFylt17VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ContextConfiguration(classes = [GyldigOpptjeningsår2020Og2021::class])
class InnvilgetOmsorgsyterMedFlestMånederTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var handler: OmsorgsarbeidMessageHandler

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

        handler.handle(
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
                                tom = YearMonth.of(2020, Month.MAY),
                                prosent = 50,
                                omsorgsmottaker = "07081812345"
                            ),
                            OmsorgsgrunnlagMelding.VedtakPeriode(
                                fom = YearMonth.of(2020, Month.JUNE),
                                tom = YearMonth.of(2020, Month.AUGUST),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),
                    OmsorgsgrunnlagMelding.Sak(
                        omsorgsyter = "04010012797",
                        vedtaksperioder = listOf(
                            OmsorgsgrunnlagMelding.VedtakPeriode(
                                fom = YearMonth.of(2020, Month.JANUARY),
                                tom = YearMonth.of(2020, Month.MAY),
                                prosent = 50,
                                omsorgsmottaker = "07081812345"
                            ),
                            OmsorgsgrunnlagMelding.VedtakPeriode(
                                fom = YearMonth.of(2020, Month.SEPTEMBER),
                                tom = YearMonth.of(2020, Month.OCTOBER),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),
                    OmsorgsgrunnlagMelding.Sak(
                        omsorgsyter = "01018212345",
                        vedtaksperioder = listOf(
                            OmsorgsgrunnlagMelding.VedtakPeriode(
                                fom = YearMonth.of(2020, Month.NOVEMBER),
                                tom = YearMonth.of(2020, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),
                ),
                rådata = RådataFraKilde("")
            )
        ).also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, it.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.Innvilget::class.java, it.utfall)

                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>() }

                it.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                    assertTrue(it.grunnlag.omsorgsyterHarFlest())
                    assertFalse(it.grunnlag.flereHarLikeMange())
                    assertEquals(
                        mapOf(
                            "12345678910" to 8,
                            "04010012797" to 7,
                            "01018212345" to 2,
                        ), it.grunnlag.yterTilAntall
                    )
                }
            }
        }
    }
}
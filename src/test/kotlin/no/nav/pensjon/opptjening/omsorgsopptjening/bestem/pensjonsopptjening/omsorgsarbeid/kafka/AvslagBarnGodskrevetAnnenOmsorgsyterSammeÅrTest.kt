package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErFylt17VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erAvslått
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsSak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertTrue

@ContextConfiguration(classes = [GyldigOpptjeningsår2020::class])
class AvslagBarnGodskrevetAnnenOmsorgsyterSammeÅrTest : SpringContextTest.NoKafka() {

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
                PdlScenario(inState = "hent annen forelder", body = "fnr_samme_fnr_gjeldende_og_historisk.json", setState = "hent barn 2"),
                PdlScenario(inState = "hent barn 2", body = "fnr_barn_2ar_2020.json"),
            )
        )

        handler.handle(
            OmsorgsGrunnlag(
                omsorgsyter = "12345678910",
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "xxx",
                kilde = Kilde.BARNETRYGD,
                omsorgsSaker = listOf(
                    OmsorgsSak(
                        omsorgsyter = "12345678910",
                        omsorgVedtakPeriode = listOf(
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2020, Month.JANUARY),
                                tom = YearMonth.of(2020, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),
                )
            ).toConsumerRecord()
        ).also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, it.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.Innvilget::class.java, it.utfall)
            }
        }

        handler.handle(
            OmsorgsGrunnlag(
                omsorgsyter = "04010012797",
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "xxx",
                kilde = Kilde.BARNETRYGD,
                omsorgsSaker = listOf(
                    OmsorgsSak(
                        omsorgsyter = "04010012797",
                        omsorgVedtakPeriode = listOf(
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2020, Month.JANUARY),
                                tom = YearMonth.of(2020, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),
                )
            ).toConsumerRecord()
        ).also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("04010012797", it.omsorgsyter)
                assertEquals("07081812345", it.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, it.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.AvslagUtenOppgave::class.java, it.utfall)

                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erAvslått<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>() }
            }
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullOmsorgForBarnUnder6Vurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.KanKunGodskrivesEnOmsorgsyterVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.KanKunGodskrivesEtBarnPerÅrVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsmottakerIkkeFylt6ArVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterFylt17ÅrVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ContextConfiguration(classes = [GyldigOpptjeningsår2020::class])
class AvslagBarnFødtOpptjeningsårDesemberIkkeTilstrekkeligAntallMånederTest : SpringContextTest.NoKafka() {

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
                PdlScenario(inState = "hent barn 1", body = "fnr_barn_0ar_des_2020.json"),
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
                                fom = YearMonth.of(2021, Month.JANUARY),
                                tom = YearMonth.of(2021, Month.DECEMBER),
                                prosent = 50,
                                omsorgsmottaker = "01122012345"
                            )
                        )
                    ),
                )
            ).toConsumerRecord()
        ).also { result ->
            result.single().also {
                assertEquals(2020, it.omsorgsAr)
                assertEquals("12345678910", it.omsorgsyter)
                assertEquals("01122012345", it.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, it.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, it.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.AvslagUtenOppgave::class.java, it.utfall)

                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterFylt17ÅrVurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsyterIkkeEldreEnn69VedUtløpAvOmsorgsårVurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<OmsorgsmottakerIkkeFylt6ArVurdering>() }
                assertTrue { it.vilkårsvurdering.erAvslått<FullOmsorgForBarnUnder6Vurdering>() }
                assertTrue { it.vilkårsvurdering.erAvslått<FullOmsorgForBarnUnder6OgIngenHarLiktAntallMånederVurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<KanKunGodskrivesEnOmsorgsyterVurdering>() }
                assertTrue { it.vilkårsvurdering.erInnvilget<KanKunGodskrivesEtBarnPerÅrVurdering>() }
            }
        }
    }
}
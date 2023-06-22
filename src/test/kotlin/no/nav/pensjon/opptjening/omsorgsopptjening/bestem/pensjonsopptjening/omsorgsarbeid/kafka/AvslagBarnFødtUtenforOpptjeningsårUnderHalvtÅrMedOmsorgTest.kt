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
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter
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
class AvslagBarnFødtUtenforOpptjeningsårUnderHalvtÅrMedOmsorgTest : SpringContextTest.NoKafka() {

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
                PdlScenario(inState = "hent barn 1", body = "fnr_barn_2ar_2020.json"),
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
                                tom = YearMonth.of(2020, Month.APRIL),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),
                )
            ).toConsumerRecord()
        ).also { result ->
            result.single().also { behandling ->
                assertEquals(2020, behandling.omsorgsAr)
                assertEquals("12345678910", behandling.omsorgsyter)
                assertEquals("07081812345", behandling.omsorgsmottaker)
                assertEquals(DomainKilde.BARNETRYGD, behandling.kilde())
                assertEquals(DomainOmsorgstype.BARNETRYGD, behandling.omsorgstype)
                assertInstanceOf(AutomatiskGodskrivingUtfall.AvslagUtenOppgave::class.java, behandling.utfall)

                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsyterErFylt17VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsyterErIkkeEldreEnn69VedUtløpAvOmsorgsår.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsmottakerHarIkkeFylt6VedUtløpAvOpptjeningsår.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erAvslått<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erAvslått<OmsorgsyterHarTilstrekkeligOmsorgsarbeidOgIngenAndreOmsorgsyterHarLikeMyeOmsorgsarbeid.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyter.Vurdering>() }
                assertTrue { behandling.vilkårsvurdering.erInnvilget<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>() }
            }
        }
    }
}
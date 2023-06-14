package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.AutomatiskGodskrivingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsSak
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import java.time.Month
import java.time.YearMonth


class OmsorgsarbeidMessageHandlerTest : SpringContextTest.NoKafka() {

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
                PdlScenario(inState = "hent barn 1", body = "fnr_barn_0ar_des_2020.json", setState = "hent barn 2"),
                PdlScenario(inState = "hent barn 2", body = "fnr_barn_2ar_2020.json")
            )
        )

        val result = handler.handle(
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
                                tom = YearMonth.of(2025, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "01122012345"
                            ),
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2021, Month.JANUARY),
                                tom = YearMonth.of(2021, Month.APRIL),
                                prosent = 50,
                                omsorgsmottaker = "07081812345"
                            ),
                            OmsorgVedtakPeriode(
                                fom = YearMonth.of(2022, Month.JANUARY),
                                tom = YearMonth.of(2022, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
                            )
                        )
                    ),

                    )
            ).toConsumerRecord()
        )
        assertEquals(8, result.count()) //1 per år per person + et ekstra for fødselsåret for 01122012345
        assertEquals(6, result.count { it.utfall is AutomatiskGodskrivingUtfall.Innvilget })
        assertEquals(2, result.count { it.utfall is AutomatiskGodskrivingUtfall.Avslag })
        assertEquals(
            listOf("01122012345"),
            result.map { it.utfall }.filterIsInstance<AutomatiskGodskrivingUtfall.Innvilget>()
                .map { it.omsorgsmottaker.fnr }.distinct()
        )
    }
}
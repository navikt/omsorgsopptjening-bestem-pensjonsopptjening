package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Topics
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsopptjeningInnvilgetMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals


class InnvilgetBarn0ÅrMayKafkaIntegrationTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo
    @Autowired
    lateinit var omsorgsopptjeningListener: OmsorgsopptjeningTopicListener
    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    companion object {
        @RegisterExtension
        private val wiremock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(SpringContextTest.WIREMOCK_PORT))
            .build()!!
    }

    @Test
    fun `consume, process and send innvilget child 0 years`() {
        wiremock.stubPdl(
            listOf(
                PdlScenario(body = "fnr_1bruk.json", setState = "hent barn"),
                PdlScenario(inState = "hent barn", body = "fnr_barn_0ar_may_2020.json")
            )
        )
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag = OmsorgsgrunnlagMelding(
                omsorgsyter = "12345678910",
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "xxx",
                kilde = Kilde.BARNETRYGD,
                saker = listOf(
                    OmsorgsgrunnlagMelding.Sak(
                        omsorgsyter = "12345678910",
                        vedtaksperioder = listOf(
                            OmsorgsgrunnlagMelding.VedtakPeriode(
                                fom = YearMonth.of(2020, Month.OCTOBER),
                                tom = YearMonth.of(2020, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "01052012345"
                            )
                        )
                    )
                ),
                rådata = RådataFraKilde("")
            )
        )

        omsorgsopptjeningListener.getFirstRecord( 10, KafkaMessageType.OPPTJENING)
            .let { record ->
                record.key().mapToClass(Topics.Omsorgsopptjening.Key::class.java).let {
                    assertEquals(
                        Topics.Omsorgsopptjening.Key(
                            ident = "12345678910"
                        ),
                        it
                    )

                }
                record.value().mapToClass(OmsorgsopptjeningInnvilgetMelding::class.java).let {
                    assertEquals(
                        OmsorgsopptjeningInnvilgetMelding(
                            omsorgsAr = 2020,
                            omsorgsyter = "12345678910",
                            omsorgsmottaker = "01052012345",
                            kilde = Kilde.BARNETRYGD,
                            omsorgstype = Omsorgstype.BARNETRYGD
                        ),
                        it
                    )
                }
                assertEquals("abc", String(record.headers().lastHeader(CorrelationId.name).value()))
            }

        assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningProducedMessageListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubPdl
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.kafka.GyldigOpptjeningsår2020
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsopptjeningInnvilget
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapToClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals

@ContextConfiguration(classes = [GyldigOpptjeningsår2020::class])
class InnvilgetBarn0ÅrMayIntegrationTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo
    @Autowired
    lateinit var omsorgsopptjeningListener: OmsorgsopptjeningProducedMessageListener

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

        omsorgsopptjeningListener.removeFirstRecord(maxSeconds = 10)
            .let { record ->
                record.key().mapToClass(OmsorgsopptjeningInnvilget.KafkaKey::class.java).let {
                    assertEquals(
                        OmsorgsopptjeningInnvilget.KafkaKey(
                            omsorgsAr = 2020,
                            omsorgsyter = "12345678910"
                        ),
                        it
                    )
                }
                record.value().mapToClass(OmsorgsopptjeningInnvilget::class.java).let {
                    assertEquals(
                        OmsorgsopptjeningInnvilget(
                            omsorgsAr = 2020,
                            omsorgsyter = "12345678910",
                            omsorgsmottaker = "01052012345",
                            kilde = Kilde.BARNETRYGD,
                            omsorgstype = Omsorgstype.BARNETRYGD
                        ),
                        it
                    )
                }
            }

        assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())
    }
}
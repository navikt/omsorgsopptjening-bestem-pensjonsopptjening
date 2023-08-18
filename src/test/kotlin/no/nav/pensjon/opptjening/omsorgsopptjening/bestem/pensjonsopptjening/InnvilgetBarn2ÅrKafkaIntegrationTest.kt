package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.BehandlingRepo
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

internal class InnvilgetBarn2ÅrKafkaIntegrationTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo
    @Autowired
    lateinit var omsorgsopptjeningListener: OmsorgsopptjeningTopicListener
    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }
    @Test
    fun `consume, process and send innvilget`() {
        wiremock.stubForPdlTransformer()
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

        sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag = OmsorgsgrunnlagMelding(
                omsorgsyter = "12345678910",
                omsorgstype = Omsorgstype.BARNETRYGD,
                kjoreHash = "xxx",
                kilde = Kilde.BARNETRYGD,
                saker =  listOf(
                    OmsorgsgrunnlagMelding.Sak(
                        omsorgsyter = "12345678910",
                        vedtaksperioder = listOf(
                            OmsorgsgrunnlagMelding.VedtakPeriode(
                                fom = YearMonth.of(2020, Month.JANUARY),
                                tom = YearMonth.of(2020, Month.DECEMBER),
                                prosent = 100,
                                omsorgsmottaker = "07081812345"
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
                            omsorgsmottaker = "07081812345",
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
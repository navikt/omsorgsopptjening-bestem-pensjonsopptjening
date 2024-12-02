package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.januar
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

@Disabled("feiler på gihub")
internal class InnvilgetBarn2ÅrKafkaIntegrationTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `konsumerer kafkamelding, prosesserer og godskriver opptjening til POPP`() {
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .willReturn(WireMock.ok())
        )

        sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag = PersongrunnlagMeldingKafka(
                omsorgsyter = "12345678910",
                persongrunnlag = listOf(
                    PersongrunnlagMeldingKafka.Persongrunnlag(
                        omsorgsyter = "12345678910",
                        omsorgsperioder = listOf(
                            PersongrunnlagMeldingKafka.Omsorgsperiode(
                                fom = januar(2020),
                                tom = desember(2020),
                                omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                omsorgsmottaker = "07081812345",
                                kilde = Kilde.BARNETRYGD,
                                utbetalt = 7234,
                                landstilknytning = Landstilknytning.NORGE
                            )
                        ),
                        hjelpestønadsperioder = emptyList(),
                    )
                ),
                rådata = Rådata(),
                innlesingId = InnlesingId.generate(),
                correlationId = CorrelationId.generate(),
            ),
        )

        Thread.sleep(2500)

        assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())

        wiremock.verify(
            WireMock.postRequestedFor(WireMock.urlEqualTo(POPP_OMSORG_PATH))
                .withRequestBody(
                    EqualToJsonPattern("""
                        {
                            "fnr":"12345678910",
                            "ar":2020,
                            "omsorgType":"OBU6",
                            "kilde":"OMSORGSOPPTJENING",
                            "fnrOmsorgFor":"07081812345"
                        }
                    """.trimIndent(),true,true)
                )
        )
    }
}
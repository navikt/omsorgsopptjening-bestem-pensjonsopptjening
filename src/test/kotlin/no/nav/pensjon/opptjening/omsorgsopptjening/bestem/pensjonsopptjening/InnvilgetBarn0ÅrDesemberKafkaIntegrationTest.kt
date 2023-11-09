package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class InnvilgetBarn0ÅrDesemberKafkaIntegrationTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @MockBean
    private lateinit var godskrivOpptjeningClient: GodskrivOpptjeningClient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    @Disabled("årsak til heng på github?")
    fun `consume, process and send innvilget child 0 years`() {
        wiremock.stubForPdlTransformer()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .willReturn(WireMock.ok())
        )
        willAnswer {
            listOf(2020, 2021)
        }.given(gyldigOpptjeningår).get()

        sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag = PersongrunnlagMeldingKafka(
                omsorgsyter = "12345678910",
                persongrunnlag = listOf(
                    PersongrunnlagMeldingKafka.Persongrunnlag(
                        omsorgsyter = "12345678910",
                        omsorgsperioder = listOf(
                            PersongrunnlagMeldingKafka.Omsorgsperiode(
                                fom = YearMonth.of(2021, Month.JANUARY),
                                tom = YearMonth.of(2021, Month.DECEMBER),
                                omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                omsorgsmottaker = "01122012345",
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
            )
        )

        Thread.sleep(2500)

        assertEquals(2, behandlingRepo.finnForOmsorgsyter("12345678910").count())

        verify(godskrivOpptjeningClient).godskriv(
            omsorgsyter = "12345678910",
            omsorgsÅr = 2020,
            omsorgstype = DomainOmsorgstype.BARNETRYGD,
            omsorgsmottaker = "01122012345"
        )
        verify(godskrivOpptjeningClient).godskriv(
            omsorgsyter = "12345678910",
            omsorgsÅr = 2021,
            omsorgstype = DomainOmsorgstype.BARNETRYGD,
            omsorgsmottaker = "01122012345"
        )
    }
}
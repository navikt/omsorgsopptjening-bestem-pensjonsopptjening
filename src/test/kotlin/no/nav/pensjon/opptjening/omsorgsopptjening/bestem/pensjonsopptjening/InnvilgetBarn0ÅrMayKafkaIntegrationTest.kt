package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningClient
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository.BehandlingRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgskategori
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.desember
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.oktober
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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.assertEquals
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

@Disabled("feiler på gihub")
class InnvilgetBarn0ÅrMayKafkaIntegrationTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var behandlingRepo: BehandlingRepo

    @MockitoBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @MockitoBean
    private lateinit var godskrivOpptjeningClient: GodskrivOpptjeningClient

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `consume, process and send innvilget child 0 years`() {
        wiremock.stubForPdlTransformer()
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()
        wiremock.givenThat(
            WireMock.post(WireMock.urlPathEqualTo(POPP_OMSORG_PATH))
                .willReturn(WireMock.ok())
        )
        willAnswer { true }.given(gyldigOpptjeningår).erGyldig(2020)

        sendOmsorgsgrunnlagKafka(
            omsorgsGrunnlag = PersongrunnlagMeldingKafka(
                omsorgsyter = "12345678910",
                persongrunnlag = listOf(
                    PersongrunnlagMeldingKafka.Persongrunnlag(
                        omsorgsyter = "12345678910",
                        omsorgsperioder = listOf(
                            PersongrunnlagMeldingKafka.Omsorgsperiode(
                                fom = oktober(2020),
                                tom = desember(2020),
                                omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                omsorgsmottaker = "01052012345",
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

        assertEquals(1, behandlingRepo.finnForOmsorgsyter("12345678910").count())

        verify(godskrivOpptjeningClient).godskriv(
            omsorgsyter = "12345678910",
            omsorgsÅr = 2020,
            omsorgstype = DomainOmsorgskategori.BARNETRYGD,
            omsorgsmottaker = "01052012345"
        )
    }
}
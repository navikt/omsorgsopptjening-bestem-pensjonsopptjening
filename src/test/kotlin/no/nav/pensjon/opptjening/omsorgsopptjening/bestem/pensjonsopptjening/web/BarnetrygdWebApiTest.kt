package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.web

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.DomainOmsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.Month.JANUARY
import java.time.Month.JUNE
import java.time.YearMonth
import java.util.*
import kotlin.test.assertFalse

class BarnetrygdWebApiTest : SpringContextTest.WithKafka() {

    @Autowired
    private lateinit var webApi: BarnetrygdWebApi

    @Autowired
    private lateinit var repo: PersongrunnlagRepo

    @Autowired
    private lateinit var handler: PersongrunnlagMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
        const val OPPTJENINGSÅR = 2020
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        willAnswer { true }
            .given(gyldigOpptjeningår).erGyldig(OPPTJENINGSÅR)
    }

    fun lagreOgProsesserMeldingSomGirOppgave(): UUID {
        val melding = repo.lagre(
            PersongrunnlagMelding.Lest(
                innhold = no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding(
                    omsorgsyter = "12345678910",
                    persongrunnlag = listOf(
                        no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag(
                            omsorgsyter = "12345678910",
                            omsorgsperioder = listOf(
                                no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Omsorgsperiode(
                                    fom = YearMonth.of(2021, JANUARY),
                                    tom = YearMonth.of(2021, JUNE),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                )
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                        no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Persongrunnlag(
                            omsorgsyter = "04010012797",
                            omsorgsperioder = listOf(
                                no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding.Omsorgsperiode(
                                    fom = YearMonth.of(2021, Month.JULY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                    omsorgsmottaker = "01122012345",
                                    kilde = Kilde.BARNETRYGD,
                                    utbetalt = 7234,
                                    landstilknytning = Landstilknytning.NORGE
                                ),
                            ),
                            hjelpestønadsperioder = emptyList(),
                        ),
                    ),
                    rådata = Rådata(),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process()!!.first().single().let { behandling ->
            behandling.assertManuell(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            Assertions.assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>()
                .let { vurdering ->
                    assertFalse(vurdering.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                    Assertions.assertTrue(vurdering.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                    Assertions.assertEquals(
                        mapOf(
                            "12345678910" to 6,
                            "04010012797" to 6,
                        ),
                        vurdering.grunnlag.data.associate { it.omsorgsyter to it.antall() }
                    )
                }
            assertThat(
                behandling.hentOppgaveopplysninger()
            ).isEqualTo(
                listOf(
                    Oppgaveopplysninger.Generell(
                        oppgavemottaker = "12345678910",
                        oppgaveTekst = """Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01122012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene."""
                    )
                ),
            )
        }
        return melding!!
    }

    @Test
    // TODO: endre så dette kun kan gjøres på feilede transaksaksjoner (utsatt pga for mye styr med testene)
    fun `kan avslutte en transaksjon`() {
        val meldingsId= lagreOgProsesserMeldingSomGirOppgave()
        repo.find(meldingsId).let { melding ->
            webApi.avsluttMelding(meldingsId)
        }
        repo.find(meldingsId).let { melding ->
            assertThat(melding.status).isInstanceOf(PersongrunnlagMelding.Status.Avsluttet::class.java)
        }
    }

    private fun FullførtBehandling.assertManuell(
        omsorgsyter: String,
        omsorgsmottaker: String,
        omsorgstype: DomainOmsorgstype = DomainOmsorgstype.BARNETRYGD,
    ): FullførtBehandling {
        assertThat(this.omsorgsAr).isEqualTo(OPPTJENINGSÅR)
        assertThat(this.omsorgsyter).isEqualTo(omsorgsyter)
        assertThat(this.omsorgsmottaker).isEqualTo(omsorgsmottaker)
        assertThat(this.omsorgstype).isEqualTo(omsorgstype)
        assertThat(this.utfall).isInstanceOf(BehandlingUtfall.Manuell::class.java)
        return this
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BarnetrygdGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.BehandlingUtfall
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.FullførtBehandling
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsyterHarTilstrekkeligOmsorgsarbeid
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.erEnesteAvslag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.finnVurdering
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.periode.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertFalse

class OmsorgsarbeidMeldingServiceTest : SpringContextTest.NoKafka() {
    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMeldingService

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
        given(gyldigOpptjeningår.get()).willReturn(listOf(OPPTJENINGSÅR))
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i året etter opptjeningsåret (uavhengig av antall måneder)`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01122012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertInstanceOf(BarnetrygdGrunnlag.FødtIOmsorgsår.FødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIDesemberOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(2021).alleMåneder(),
                            it.omsorgsmåneder
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret gis det ingen opptjening dersom omsorgsyter ikke har ytt omsorg i året etter omsorgsåret`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2022, Month.JANUARY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01122012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )
        assertTrue(handler.process().isEmpty())
    }

    @Test
    fun `gitt et barn født i andre måneder enn desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i opptjeningsåret (uavhengig av antall måneder) - 3 måneder`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.OCTOBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01052012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345"
            )
            assertInstanceOf(BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(
                                YearMonth.of(2020, Month.OCTOBER),
                                YearMonth.of(2020, Month.DECEMBER)
                            ).alleMåneder(),
                            it.omsorgsmåneder
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født i andre måneder enn desember i opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt omsorg i opptjeningsåret (uavhengig av antall måneder) - 7 måneder`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.MAY),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01052012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01052012345"
            )
            assertInstanceOf(BarnetrygdGrunnlag.FødtIOmsorgsår.IkkeFødtDesember::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtIOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(
                                YearMonth.of(2020, Month.MAY),
                                YearMonth.of(2020, Month.DECEMBER)
                            ).alleMåneder(),
                            it.omsorgsmåneder
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det innvilges opptjening dersom omsorgsyter har ytt et halvt år eller mer omsorg`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2018, Month.SEPTEMBER),
                                    tom = YearMonth.of(2025, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(BarnetrygdGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(2020).alleMåneder(),
                            it.omsorgsmåneder
                        )
                    }
                }
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det avslås opptjening dersom omsorgsyter har ytt mindre enn et halvt år omsorg - full omsorg`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(BarnetrygdGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(YearMonth.of(2020, Month.JANUARY), YearMonth.of(2020, Month.MAY)).alleMåneder(),
                            it.omsorgsmåneder
                        )
                    }
                }
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>())
        }
    }

    @Test
    fun `gitt et barn født utenfor opptjeningsåret skal det avslås opptjening dersom omsorgsyter har ytt mindre enn et halvt år omsorg - delt omsorg`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertInstanceOf(BarnetrygdGrunnlag.IkkeFødtIOmsorgsår::class.java, behandling.grunnlag)
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>()
                .also { vurdering ->
                    assertInstanceOf(
                        OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Grunnlag.OmsorgsmottakerFødtUtenforOmsorgsår::class.java,
                        vurdering.grunnlag
                    ).also {
                        assertEquals(
                            Periode(YearMonth.of(2020, Month.JANUARY), YearMonth.of(2020, Month.MAY)).alleMåneder(),
                            it.omsorgsmåneder
                        )
                    }
                }
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarTilstrekkeligOmsorgsarbeid.Vurdering>())
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest måneder omsorg i opptjeningsåret - innvilget for omsorgsyter under behandling`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.AUGUST),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.OCTOBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "01018212345",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.NOVEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertTrue(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertFalse(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "12345678910" to 8,
                        "04010012797" to 7,
                        "01018212345" to 2,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter.fnr to it.antall() }
                )
            }
        }
    }

    @Test
    fun `gitt at flere omsorgytere har ytt omsorg for samme barn, innvilges opptjening til omsorgsyter med flest måneder omsorg i opptjeningsåret - avslag for omsorgsyter under behandling`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.SEPTEMBER),
                                    tom = YearMonth.of(2020, Month.OCTOBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.MAY),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.AUGUST),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "01018212345",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.NOVEMBER),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertFalse(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "04010012797" to 8,
                        "12345678910" to 7,
                        "01018212345" to 2,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter.fnr to it.antall() }
                )
                assertTrue(it.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            }
        }
    }

    @Test
    fun `gitt at en omsorgyter har ytt omsorg for flere barn i det samme året, innvilges opptjening for det eldste barnet hvor alle kriterer er oppfylt`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01052012345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JANUARY),
                                    prosent = 100,
                                    omsorgsmottaker = "01122012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().also { behandlinger ->
            assertEquals(3, behandlinger.count())
            behandlinger[0].also {
                it.assertInnvilget(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "07081812345"
                )
            }
            behandlinger[1].also {
                it.assertAvslag(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "01052012345"
                )
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
            }
            behandlinger[2].also {
                it.assertAvslag(
                    omsorgsyter = "12345678910",
                    omsorgsmottaker = "01122012345"
                )
                assertTrue(it.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
            }
        }
    }

    @Test
    fun `gitt et barn født i desember i opptjeningsåret og flere omsorgsyter har ytt omsorg for samme barn avslås opptjening dersom en annen omsorgsyter har flere måneder med omsorg i året etter opptjeningsåret`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.FEBRUARY),
                                    prosent = 50,
                                    omsorgsmottaker = "01122012345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.FEBRUARY),
                                    prosent = 50,
                                    omsorgsmottaker = "01122012345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.MARCH),
                                    tom = YearMonth.of(2021, Month.MAY),
                                    prosent = 100,
                                    omsorgsmottaker = "01122012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertFalse(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "04010012797" to 5,
                        "12345678910" to 2,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter.fnr to it.antall() }
                )
            }
        }
    }

    @Test
    fun `gitt at en omsorgsyter har fått innvilget opptjening for en omsorgsmottaker, kan ikke andre omsorgstyere innvilges for den samme omsrogsmottakeren det samme året`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
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
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().assertInnvilget(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345"
        )

        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "04010012797",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().also { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "04010012797",
                omsorgsmottaker = "07081812345",
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesEnOmsorgsyterPerÅr.Vurdering>())
        }
    }

    @Test
    fun `gitt at en omsorgsyter er innvilget opptjening for en omsorgsmottaker, kan ikke den samme omsorgsyteren innvilges opptjening for andre omsorgsmottakere samme år`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01052012345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().also { behandlinger ->
            assertEquals(2, behandlinger.count())
            behandlinger.first().assertInnvilget(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            behandlinger.last().assertAvslag(
                "12345678910",
                "01052012345"
            )
            assertTrue(behandlinger.last().vilkårsvurdering.erEnesteAvslag<OmsorgsopptjeningKanKunGodskrivesForEtBarnPerÅr.Vurdering>())
        }
    }

    @Test
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal opptjening avslås - omsorgsmottaker født uten for opptjeningsår`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JULY),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "01018212345",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JULY),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().let { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "07081812345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                kotlin.test.assertTrue(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "12345678910" to 7,
                        "04010012797" to 7,
                        "01018212345" to 7,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter.fnr to it.antall() }
                )
            }
            assertNull(//forventer ikke oppgave siden omsorgstyer ikke mottok i desember
                behandling.opprettOppgave(
                    oppgaveEksistererForOmsorgsyter = { _: String, _: Int -> false },
                    oppgaveEksistererForOmsorgsmottaker = { _: String, _: Int -> false }
                )
            )
        }
    }

    @Test
    fun `gitt at flere omsorgsytere har ytt omsorg for samme omsorgsmottaker i like mange måneder i opptjeningsåret skal opptjening avslås - omsorgsmottager født i desember i opptjeningsåret`() {
        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.JUNE),
                                    prosent = 100,
                                    omsorgsmottaker = "01122012345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JULY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "01122012345"
                                ),
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().let { behandling ->
            behandling.assertAvslag(
                omsorgsyter = "12345678910",
                omsorgsmottaker = "01122012345"
            )
            assertTrue(behandling.vilkårsvurdering.erEnesteAvslag<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>())
            behandling.vilkårsvurdering.finnVurdering<OmsorgsyterHarMestOmsorgAvAlleOmsorgsytere.Vurdering>().let {
                assertFalse(it.grunnlag.omsorgsyterHarFlestOmsorgsmåneder())
                assertTrue(it.grunnlag.omsorgsyterErEnAvFlereMedFlestOmsorgsmåneder())
                assertEquals(
                    mapOf(
                        "12345678910" to 6,
                        "04010012797" to 6,
                    ),
                    it.grunnlag.data.associate { it.omsorgsyter.fnr to it.antall() }
                )
            }
            assertInstanceOf(//forventer oppgave siden omsorgsyter mokkok i desember
                Oppgave::class.java, behandling.opprettOppgave(
                    oppgaveEksistererForOmsorgsyter = { _: String, _: Int -> false },
                    oppgaveEksistererForOmsorgsmottaker = { _: String, _: Int -> false }
                )).also {
                assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår::class.java, it.detaljer)
            }
        }
    }

    @Test
    fun `gitt en omsorgsmottaker som har blitt innvilget for en omsorgsyter i et år, kan innvilges for en annen omsorgsyter et annet år`() {
        given(gyldigOpptjeningår.get()).willReturn(listOf(2020, 2021))

        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "12345678910",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
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
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )


        handler.process().single().assertInnvilget(
            omsorgsyter = "12345678910",
            omsorgsmottaker = "07081812345",
            år = OPPTJENINGSÅR,
        )

        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "04010012797",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2021, Month.JANUARY),
                                    tom = YearMonth.of(2021, Month.DECEMBER),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                    ),
                    rådata = RådataFraKilde(""),
                    innlesingId = InnlesingId.generate(),
                    correlationId = CorrelationId.generate(),
                )
            ),
        )

        handler.process().single().assertInnvilget(
            omsorgsyter = "04010012797",
            omsorgsmottaker = "07081812345",
            år = 2021,
        )
    }

    private fun FullførtBehandling.assertInnvilget(
        omsorgsyter: String,
        omsorgsmottaker: String,
        år: Int = OPPTJENINGSÅR
    ) {
        assertEquals(år, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(DomainKilde.BARNETRYGD, this.kilde())
        assertEquals(DomainOmsorgstype.BARNETRYGD, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Innvilget::class.java, this.utfall)
    }

    private fun FullførtBehandling.assertAvslag(omsorgsyter: String, omsorgsmottaker: String) {
        assertEquals(OPPTJENINGSÅR, this.omsorgsAr)
        assertEquals(omsorgsyter, this.omsorgsyter)
        assertEquals(omsorgsmottaker, this.omsorgsmottaker)
        assertEquals(DomainKilde.BARNETRYGD, this.kilde())
        assertEquals(DomainOmsorgstype.BARNETRYGD, this.omsorgstype)
        assertInstanceOf(BehandlingUtfall.Avslag::class.java, this.utfall)
    }
}
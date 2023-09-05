package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.Oppgave
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.model.OppgaveDetaljer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.willAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Month
import java.time.YearMonth


class OppgaveopprettelseTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var repo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var handler: OmsorgsarbeidMeldingService

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår skal det opprettes oppgave for den som mottok i desember`() {
        wiremock.stubForPdlTransformer()
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

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
                                    tom = YearMonth.of(2020, Month.JUNE),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JULY),
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
            assertFalse(behandling.erInnvilget())
            assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForMelding(behandling.meldingId))
            assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForBehandling(behandling.id))
        }

        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "04010012797",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.JUNE),
                                    prosent = 100,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JULY),
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
            assertFalse(behandling.erInnvilget())
            oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                assertEquals(oppgave, oppgaveRepo.findForBehandling(behandling.id).single())
                assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java, oppgave.detaljer).also {
                    assertEquals(it.omsorgsyter, "04010012797")
                    assertEquals(it.omsorgsmottaker, "07081812345")
                    assertEquals(it.annenOmsorgsyter, "12345678910")
                    assertEquals(
                        """
                    Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 12345678910 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.
                """.trimIndent(), it.oppgaveTekst
                    )
                }
            }
        }
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår kan det opprettes oppgave for begge dersom en av foreldrene inngår i en annen familiekonstellasjon hvor oppgave også skal opprettes`() {
        wiremock.stubForPdlTransformer()
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

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
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
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
            assertFalse(behandling.erInnvilget())
            oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                assertEquals(oppgave, oppgaveRepo.findForBehandling(behandling.id).single())
                assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java, oppgave.detaljer).also {
                    assertEquals(it.omsorgsyter, "12345678910")
                    assertEquals(it.omsorgsmottaker, "07081812345")
                    assertEquals(it.annenOmsorgsyter, "04010012797")
                    assertEquals(
                        """
                    Godskr. omsorgspoeng, flere mottakere: Flere personer har mottatt barnetrygd samme år for barnet under 6 år med fnr 07081812345. Den bruker som oppgaven gjelder mottok barnetrygd i minst seks måneder, og hadde barnetrygd i desember måned. Bruker med fnr 04010012797 mottok også barnetrygd for 6 måneder i samme år. Vurder hvem som skal ha omsorgspoengene.
                """.trimIndent(), it.oppgaveTekst
                    )
                }
            }
        }

        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "04010012797",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                ),
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
                                    omsorgsmottaker = "01052012345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "01018212345",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JUNE),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
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
            oppgaveRepo.findForMelding(behandlinger[0].meldingId).single().also { oppgave ->
                assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForBehandling(behandlinger[0].id))
                assertEquals(oppgave, oppgaveRepo.findForBehandling(behandlinger[1].id).single())
                assertInstanceOf(
                    OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorgIFødselsår::class.java,
                    oppgave.detaljer
                ).also {
                    assertEquals(it.omsorgsyter, "04010012797")
                    assertEquals(it.omsorgsmottaker, "01052012345")
                    assertEquals(
                        it.oppgaveTekst,
                        """Godskr. omsorgspoeng, flere mottakere: Flere personer som har mottatt barnetrygd samme år for barnet med fnr 01052012345 i barnets fødselsår. Vurder hvem som skal ha omsorgspoengene.""".trimIndent()
                    )
                }
            }
        }
    }

    @Test
    fun `gitt to omsorgsytere med like mange omsorgsmåneder for samme barn i samme omsorgsår skal det opprettes oppgave for den som mottok i desember - hvis begge mottok i desember opprettes det for en av partene`() {
        wiremock.stubForPdlTransformer()
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

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
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
            assertFalse(behandling.erInnvilget())
            oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                assertEquals(oppgave, oppgaveRepo.findForBehandling(behandling.id).single())
                assertInstanceOf(OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java, oppgave.detaljer).also {
                    assertEquals(it.omsorgsyter, "12345678910")
                    assertEquals(it.omsorgsmottaker, "07081812345")
                    assertEquals(it.annenOmsorgsyter, "04010012797")
                }
            }
        }

        repo.persist(
            OmsorgsarbeidMelding(
                innhold = OmsorgsgrunnlagMelding(
                    omsorgsyter = "04010012797",
                    omsorgstype = Omsorgstype.BARNETRYGD,
                    kilde = Kilde.BARNETRYGD,
                    saker = listOf(
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "12345678910",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
                                    prosent = 50,
                                    omsorgsmottaker = "07081812345"
                                )
                            )
                        ),
                        OmsorgsgrunnlagMelding.Sak(
                            omsorgsyter = "04010012797",
                            vedtaksperioder = listOf(
                                OmsorgsgrunnlagMelding.VedtakPeriode(
                                    fom = YearMonth.of(2020, Month.JANUARY),
                                    tom = YearMonth.of(2020, Month.DECEMBER),
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
            assertFalse(behandling.erInnvilget())
            assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForMelding(behandling.meldingId))
            assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForBehandling(behandling.id))
        }
    }

    @Test
    fun `gitt at to omsorgsytere har like mange omsorgsmåneder for flere barn innenfor samme omsorgsår opprettes det bare oppgave for det eldste barnet`() {
        wiremock.stubForPdlTransformer()
        willAnswer {
            listOf(2020)
        }.given(gyldigOpptjeningår).get()

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
                                        fom = YearMonth.of(2021, Month.JULY),
                                        tom = YearMonth.of(2021, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    ),
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
                                        prosent = 50,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.JUNE),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    ),
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2020, Month.DECEMBER),
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

        handler.process().let { result ->
            assertEquals(2, result.count())
            result.first().also { behandling ->
                assertFalse(behandling.erInnvilget())
                oppgaveRepo.findForMelding(behandling.meldingId).single().also { oppgave ->
                    assertEquals(oppgave, oppgaveRepo.findForBehandling(behandling.id).single())
                    assertInstanceOf(
                        OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java,
                        oppgave.detaljer
                    ).also {
                        assertEquals(it.omsorgsyter, "12345678910")
                        assertEquals(it.omsorgsmottaker, "07081812345")
                        assertEquals(it.annenOmsorgsyter, "04010012797")
                    }
                }
            }
            result.last().also { behandling ->
                assertFalse(behandling.erInnvilget())
                assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForBehandling(behandling.id))
                assertEquals(1, oppgaveRepo.findForMelding(behandling.meldingId).count())
            }
        }
    }

    @Test
    fun `gitt at to omsorgsytere har like mange omsorgsmåneder for flere barn i forskjellige omsorgsår opprettes det oppgave for det eldste barnet i hvert omsorgsår`() {
        wiremock.stubForPdlTransformer()
        willAnswer {
            listOf(2020, 2021)
        }.given(gyldigOpptjeningår).get()

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
                                        fom = YearMonth.of(2021, Month.JULY),
                                        tom = YearMonth.of(2021, Month.DECEMBER),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    ),
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.DECEMBER),
                                        prosent = 50,
                                        omsorgsmottaker = "07081812345"
                                    )
                                )
                            ),
                            OmsorgsgrunnlagMelding.Sak(
                                omsorgsyter = "04010012797",
                                vedtaksperioder = listOf(
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.JUNE),
                                        prosent = 100,
                                        omsorgsmottaker = "01122012345"
                                    ),
                                    OmsorgsgrunnlagMelding.VedtakPeriode(
                                        fom = YearMonth.of(2020, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.DECEMBER),
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

        handler.process().let { result ->
            assertEquals(4, result.count())
            result[0].also { behandling ->
                assertFalse(behandling.erInnvilget())
                assertEquals(2020, behandling.omsorgsAr)
                assertEquals("07081812345", behandling.omsorgsmottaker)
                oppgaveRepo.findForMelding(behandling.meldingId)[0].also { oppgave ->
                    assertEquals(oppgave, oppgaveRepo.findForBehandling(behandling.id).single())
                    assertInstanceOf(
                        OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java,
                        oppgave.detaljer
                    ).also {
                        assertEquals(it.omsorgsyter, "12345678910")
                        assertEquals(it.omsorgsmottaker, "07081812345")
                        assertEquals(it.annenOmsorgsyter, "04010012797")
                    }
                }
            }
            result[1].also { behandling ->
                assertFalse(behandling.erInnvilget())
                assertEquals(2021, behandling.omsorgsAr)
                assertEquals("07081812345", behandling.omsorgsmottaker)
                oppgaveRepo.findForMelding(behandling.meldingId)[1].also { oppgave ->
                    assertEquals(oppgave, oppgaveRepo.findForBehandling(behandling.id).single())
                    assertInstanceOf(
                        OppgaveDetaljer.FlereOmsorgytereMedLikeMyeOmsorg::class.java,
                        oppgave.detaljer
                    ).also {
                        assertEquals(it.omsorgsyter, "12345678910")
                        assertEquals(it.omsorgsmottaker, "07081812345")
                        assertEquals(it.annenOmsorgsyter, "04010012797")
                    }
                }
            }
            result[2].also { behandling ->
                assertFalse(behandling.erInnvilget())
                assertEquals(2020, behandling.omsorgsAr)
                assertEquals("01122012345", behandling.omsorgsmottaker)
                assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForBehandling(behandling.id))
                assertEquals(2, oppgaveRepo.findForMelding(behandling.meldingId).count())
            }
            result[3].also { behandling ->
                assertFalse(behandling.erInnvilget())
                assertEquals(2021, behandling.omsorgsAr)
                assertEquals("01122012345", behandling.omsorgsmottaker)
                assertEquals(emptyList<Oppgave>(), oppgaveRepo.findForBehandling(behandling.id))
                assertEquals(2, oppgaveRepo.findForMelding(behandling.meldingId).count())
            }
        }
    }
}
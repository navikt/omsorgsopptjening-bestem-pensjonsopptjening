package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.OmsorgsarbeidMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.OmsorgsgrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.transaction.support.TransactionTemplate
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertNull

class ProsesseringsParallellitetTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var omsorgsarbeidRepo: OmsorgsarbeidRepo

    @Autowired
    private lateinit var omsorgsarbeidMeldingService: OmsorgsarbeidMeldingService

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @MockBean
    private lateinit var gyldigOpptjeningår: GyldigOpptjeningår

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        given(gyldigOpptjeningår.get()).willReturn(listOf(2020))
    }

    @Nested
    inner class GodskrivOpptjening {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            omsorgsarbeidRepo.persist(
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
            omsorgsarbeidMeldingService.process()

            transactionTemplate.execute {
                //låser den aktuelle raden for denne transaksjonens varighet
                Assertions.assertNotNull(godskrivOpptjeningRepo.finnNesteUprosesserte())

                //opprett ny transaksjon mens den forrige fortsatt lever
                transactionTemplate.execute {
                    //skal ikke finne noe siden raden er låst pga "select for update skip locked"
                    Assertions.assertNull(godskrivOpptjeningRepo.finnNesteUprosesserte())
                }
                //fortsatt samme transaksjon
                Assertions.assertNotNull(godskrivOpptjeningRepo.finnNesteUprosesserte())
            } //rad ikke låst lenger ved transaksjon slutt


            //ny transaksjon finner raden da den ikke lenger er låst
            transactionTemplate.execute {
                Assertions.assertNotNull(godskrivOpptjeningRepo.finnNesteUprosesserte())
            }
        }
    }


    @Nested
    inner class Omsorgsarbeid {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            omsorgsarbeidRepo.persist(
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

            transactionTemplate.execute {
                //låser den aktuelle raden for denne transaksjonens varighet
                Assertions.assertNotNull(omsorgsarbeidRepo.finnNesteUprosesserte())

                //opprett ny transaksjon mens den forrige fortsatt lever
                transactionTemplate.execute {
                    //skal ikke finne noe siden raden er låst pga "select for update skip locked"
                    assertNull(omsorgsarbeidRepo.finnNesteUprosesserte())
                }
                //fortsatt samme transaksjon
                Assertions.assertNotNull(omsorgsarbeidRepo.finnNesteUprosesserte())
            } //rad ikke låst lenger ved transaksjon slutt


            //ny transaksjon finner raden da den ikke lenger er låst
            transactionTemplate.execute {
                Assertions.assertNotNull(omsorgsarbeidRepo.finnNesteUprosesserte())
            }
        }
    }

    @Nested
    inner class Oppgave {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            omsorgsarbeidRepo.persist(
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
            omsorgsarbeidMeldingService.process()

            transactionTemplate.execute {
                //låser den aktuelle raden for denne transaksjonens varighet
                Assertions.assertNotNull(oppgaveRepo.finnNesteUprosesserte())

                //opprett ny transaksjon mens den forrige fortsatt lever
                transactionTemplate.execute {
                    //skal ikke finne noe siden raden er låst pga "select for update skip locked"
                    Assertions.assertNull(oppgaveRepo.finnNesteUprosesserte())
                }
                //fortsatt samme transaksjon
                Assertions.assertNotNull(oppgaveRepo.finnNesteUprosesserte())
            } //rad ikke låst lenger ved transaksjon slutt


            //ny transaksjon finner raden da den ikke lenger er låst
            transactionTemplate.execute {
                Assertions.assertNotNull(oppgaveRepo.finnNesteUprosesserte())
            }
        }
    }
}
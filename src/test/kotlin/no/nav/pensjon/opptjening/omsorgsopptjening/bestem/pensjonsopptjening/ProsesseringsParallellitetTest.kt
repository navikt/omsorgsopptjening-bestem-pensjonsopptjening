package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.GyldigOpptjeningår
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.RådataFraKilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.MedlemIFolketrygden
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
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

class ProsesseringsParallellitetTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingService: PersongrunnlagMeldingService

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
            persongrunnlagRepo.persist(
                PersongrunnlagMelding.Lest(
                    innhold = PersongrunnlagMeldingKafka(
                        omsorgsyter = "12345678910",
                        persongrunnlag = listOf(
                            PersongrunnlagMeldingKafka.Persongrunnlag(
                                omsorgsyter = "12345678910",
                                omsorgsperioder = listOf(
                                    PersongrunnlagMeldingKafka.Omsorgsperiode(
                                        fom = YearMonth.of(2018, Month.SEPTEMBER),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                        omsorgsmottaker = "07081812345",
                                        kilde = Kilde.BARNETRYGD,
                                        medlemskap = MedlemIFolketrygden.Ukjent,
                                        utbetalt = 7234
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
            persongrunnlagMeldingService.process()

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
            persongrunnlagRepo.persist(
                PersongrunnlagMelding.Lest(
                    innhold = PersongrunnlagMeldingKafka(
                        omsorgsyter = "12345678910",
                        persongrunnlag = listOf(
                            PersongrunnlagMeldingKafka.Persongrunnlag(
                                omsorgsyter = "12345678910",
                                omsorgsperioder = listOf(
                                    PersongrunnlagMeldingKafka.Omsorgsperiode(
                                        fom = YearMonth.of(2018, Month.SEPTEMBER),
                                        tom = YearMonth.of(2025, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                        omsorgsmottaker = "07081812345",
                                        kilde = Kilde.BARNETRYGD,
                                        medlemskap = MedlemIFolketrygden.Ukjent,
                                        utbetalt = 7234
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
                Assertions.assertNotNull(persongrunnlagRepo.finnNesteUprosesserte())

                //opprett ny transaksjon mens den forrige fortsatt lever
                transactionTemplate.execute {
                    //skal ikke finne noe siden raden er låst pga "select for update skip locked"
                    assertNull(persongrunnlagRepo.finnNesteUprosesserte())
                }
                //fortsatt samme transaksjon
                Assertions.assertNotNull(persongrunnlagRepo.finnNesteUprosesserte())
            } //rad ikke låst lenger ved transaksjon slutt


            //ny transaksjon finner raden da den ikke lenger er låst
            transactionTemplate.execute {
                Assertions.assertNotNull(persongrunnlagRepo.finnNesteUprosesserte())
            }
        }
    }

    @Nested
    inner class Oppgave {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            persongrunnlagRepo.persist(
                PersongrunnlagMelding.Lest(
                    innhold = PersongrunnlagMeldingKafka(
                        omsorgsyter = "12345678910",
                        persongrunnlag = listOf(
                            PersongrunnlagMeldingKafka.Persongrunnlag(
                                omsorgsyter = "12345678910",
                                omsorgsperioder = listOf(
                                    PersongrunnlagMeldingKafka.Omsorgsperiode(
                                        fom = YearMonth.of(2021, Month.JANUARY),
                                        tom = YearMonth.of(2021, Month.JUNE),
                                        omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                        omsorgsmottaker = "01122012345",
                                        kilde = Kilde.BARNETRYGD,
                                        medlemskap = MedlemIFolketrygden.Ukjent,
                                        utbetalt = 7234
                                    )
                                )
                            ),
                            PersongrunnlagMeldingKafka.Persongrunnlag(
                                omsorgsyter = "04010012797",
                                omsorgsperioder = listOf(
                                    PersongrunnlagMeldingKafka.Omsorgsperiode(
                                        fom = YearMonth.of(2021, Month.JULY),
                                        tom = YearMonth.of(2021, Month.DECEMBER),
                                        omsorgstype = Omsorgstype.FULL_BARNETRYGD,
                                        omsorgsmottaker = "01122012345",
                                        kilde = Kilde.BARNETRYGD,
                                        medlemskap = MedlemIFolketrygden.Ukjent,
                                        utbetalt = 7234
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
            persongrunnlagMeldingService.process()

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
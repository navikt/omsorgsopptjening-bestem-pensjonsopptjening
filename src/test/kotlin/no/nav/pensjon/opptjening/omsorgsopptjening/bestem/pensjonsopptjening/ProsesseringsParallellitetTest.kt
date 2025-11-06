package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.SpringContextTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeAlderspensjon
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenLøpendeUføretrgyd
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.ingenUnntaksperioderForMedlemskap
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.stubForPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.wiremockWithPdlTransformer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.godskriv.model.GodskrivOpptjeningRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.oppgave.repository.OppgaveRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMelding
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.model.PersongrunnlagMeldingProcessingService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.persongrunnlag.repository.PersongrunnlagRepo
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils.NewTransactionTemplate
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.CorrelationId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.InnlesingId
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.Rådata
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.Omsorgstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.time.Month
import java.time.YearMonth
import java.util.UUID
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.domene.PersongrunnlagMelding as PersongrunnlagMeldingKafka

@TestPropertySource(
    properties = ["GYLDIG_OPPTJENINGSAR=2020"]
)
class ProsesseringsParallellitetTest : SpringContextTest.NoKafka() {

    @Autowired
    private lateinit var persongrunnlagRepo: PersongrunnlagRepo

    @Autowired
    private lateinit var persongrunnlagMeldingService: PersongrunnlagMeldingProcessingService

    @Autowired
    private lateinit var godskrivOpptjeningRepo: GodskrivOpptjeningRepo

    @Autowired
    private lateinit var oppgaveRepo: OppgaveRepo

    @Autowired
    private lateinit var transactionTemplate: NewTransactionTemplate

    companion object {
        @JvmField
        @RegisterExtension
        val wiremock = wiremockWithPdlTransformer()
    }

    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()
        wiremock.stubForPdlTransformer()
        wiremock.ingenUnntaksperioderForMedlemskap()
        wiremock.ingenLøpendeAlderspensjon()
        wiremock.ingenLøpendeUføretrgyd()
    }

    @Nested
    inner class GodskrivOpptjening {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            persongrunnlagRepo.lagre(
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
                                        utbetalt = 7234,
                                        landstilknytning = Landstilknytning.NORGE
                                    )
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
            persongrunnlagMeldingService.process()

            transactionTemplate.execute {
                //låser den aktuelle raden for denne transaksjonens varighet
                assertNotNull(godskrivOpptjeningRepo.finnNesteUprosesserte(5))

                //opprett ny transaksjon mens den forrige fortsatt lever
                transactionTemplate.execute {
                    //skal ikke finne noe siden raden er låst pga "select for update skip locked"
                    assertThat(godskrivOpptjeningRepo.finnNesteUprosesserte(5).data).isNullOrEmpty()
                }
                //fortsatt samme transaksjon
                assertNotNull(godskrivOpptjeningRepo.finnNesteUprosesserte(5))
            } //rad ikke låst lenger ved transaksjon slutt


            //ny transaksjon finner raden da den ikke lenger er låst
            transactionTemplate.execute {
                assertNotNull(godskrivOpptjeningRepo.finnNesteUprosesserte(5))
            }
        }
    }


    @Nested
    inner class Omsorgsarbeid {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            persongrunnlagRepo.lagre(
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
                                        utbetalt = 7234,
                                        landstilknytning = Landstilknytning.NORGE
                                    )
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

            transactionTemplate.execute {
                //låser den aktuelle raden for denne transaksjonens varighet
                assertNotNull(persongrunnlagRepo.finnNesteKlarTilProsessering(UUID.randomUUID(), 5))

                //opprett ny transaksjon mens den forrige fortsatt lever
                transactionTemplate.execute {
                    //skal ikke finne noe siden raden er låst pga "select for update skip locked"
                    assertThat(persongrunnlagRepo.finnNesteKlarTilProsessering(UUID.randomUUID(), 5)).isNullOrEmpty()
                }
                //fortsatt samme transaksjon
                assertNotNull(persongrunnlagRepo.finnNesteKlarTilProsessering(UUID.randomUUID(), 5))
            } //rad ikke låst lenger ved transaksjon slutt


            //ny transaksjon finner raden da den ikke lenger er låst
            transactionTemplate.execute {
                assertNotNull(persongrunnlagRepo.finnNesteKlarTilProsessering(UUID.randomUUID(), 5))
            }
        }
    }

    @Nested
    inner class Oppgave {
        @Test
        fun `finnNesteUprosesserte låser raden slik at den ikke plukkes opp av andre connections`() {
            persongrunnlagRepo.lagre(
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
                                        utbetalt = 7234,
                                        landstilknytning = Landstilknytning.NORGE
                                    )
                                ),
                                hjelpestønadsperioder = emptyList(),
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
            persongrunnlagMeldingService.process()

            val uttrekk1 = oppgaveRepo.finnNesteUprosesserte(5)
            val uttrekk2 = oppgaveRepo.finnNesteUprosesserte(5)
            oppgaveRepo.frigi(uttrekk1)
            val uttrekk3 = oppgaveRepo.finnNesteUprosesserte(5)
            oppgaveRepo.frigi(uttrekk2)
            oppgaveRepo.frigi(uttrekk3)

            assertThat(uttrekk1.data).isNotEmpty()
            assertThat(uttrekk2.data).isNullOrEmpty()
            assertThat(uttrekk3.data).isNotEmpty()
        }
    }
}
package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlFnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Kilde
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Landstilknytning
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Omsorgstype
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Month
import java.time.YearMonth
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgVedtakPeriode as KafkaOmsorgVedtakPeriode
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsGrunnlag as KafkaOmsorgsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsSak as KafkaOmsorgsSak


@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsArbeidServiceTest {
    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var omsorgsArbeidService: OmsorgsArbeidService

    @MockBean
    private lateinit var pdlService: PdlService

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()

        `when`(pdlService.hentPerson(omsorgsYter1.gjeldendeFnr)).thenReturn(omsorgsYter1)
        `when`(pdlService.hentPerson(omsorgsYter2.gjeldendeFnr)).thenReturn(omsorgsYter2)

        `when`(pdlService.hentPerson(omsorgsmottaker1.gjeldendeFnr)).thenReturn(omsorgsmottaker1)
        `when`(pdlService.hentPerson(omsorgsmottaker2.gjeldendeFnr)).thenReturn(omsorgsmottaker2)
    }

    @Test
    fun `Given a snapshot without any other parties then return snapshot without other parties`() {
        val kafkaMessage = createKafkaMessage(
            omsorgsAr = 2020,
            omsorgsyter = omsorgsYter1.gjeldendeFnr,
            omsorgstype = Omsorgstype.BARNETRYGD,
            omsorgsarbeidVedtak = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(2020, Month.JANUARY),
                    tom = YearMonth.of(2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter1.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )

        val snapshot = omsorgsArbeidService.createAndSaveOmsorgasbeidsSnapshot(kafkaMessage)

        assertEquals(omsorgsYter1.gjeldendeFnr, snapshot.omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(0, omsorgsArbeidService.relaterteSnapshot(snapshot).size)
    }

    @Test
    fun `Given a snapshot with other parties but no snapshot is stored in DB then return snapshot without other parties`() {
        val kafkaMessage = createKafkaMessage(
            omsorgsAr = OMSORGS_AR_2020,
            omsorgsyter = omsorgsYter1.gjeldendeFnr,
            omsorgstype = Omsorgstype.BARNETRYGD,
            omsorgsarbeidVedtak = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(OMSORGS_AR_2020, Month.JANUARY),
                    tom = YearMonth.of(OMSORGS_AR_2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter2.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )

        val snapshot = omsorgsArbeidService.createAndSaveOmsorgasbeidsSnapshot(kafkaMessage)

        assertEquals(omsorgsYter1.gjeldendeFnr, snapshot.omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(0, omsorgsArbeidService.relaterteSnapshot(snapshot).size)
    }

    @Test
    fun `Given a snapshot with other parties that is stored in DB then return snapshot with other parties`() {
        val kafkaMessage1 = createKafkaMessage(
            omsorgsAr = OMSORGS_AR_2020,
            omsorgsyter = omsorgsYter1.gjeldendeFnr,
            omsorgstype = Omsorgstype.BARNETRYGD,
            omsorgsarbeidVedtak = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(OMSORGS_AR_2020, Month.JANUARY),
                    tom = YearMonth.of(OMSORGS_AR_2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter1.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )

        val kafkaMessage2 = createKafkaMessage(
            omsorgsAr = OMSORGS_AR_2020,
            omsorgsyter = omsorgsYter2.gjeldendeFnr,
            omsorgstype = Omsorgstype.BARNETRYGD,
            omsorgsarbeidVedtak = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(OMSORGS_AR_2020, Month.JANUARY),
                    tom = YearMonth.of(OMSORGS_AR_2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter1.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )

        val snapshot1 = omsorgsArbeidService.createAndSaveOmsorgasbeidsSnapshot(kafkaMessage1)
        val snapshot2 = omsorgsArbeidService.createAndSaveOmsorgasbeidsSnapshot(kafkaMessage2)
        val relatedToSnapshot2 = omsorgsArbeidService.relaterteSnapshot(snapshot2)

        assertEquals(omsorgsYter2.gjeldendeFnr, snapshot2.omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(omsorgsYter1.gjeldendeFnr, snapshot1.omsorgsyter.gjeldendeFnr.fnr)

        assertEquals(1, relatedToSnapshot2.size)
        assertEquals(snapshot1.id, relatedToSnapshot2.first().id)
    }

    private fun createKafkaMessage(
        omsorgsAr: Int,
        omsorgsyter: String,
        omsorgstype: Omsorgstype,
        omsorgsarbeidVedtak: List<KafkaOmsorgVedtakPeriode>
    ): KafkaOmsorgsGrunnlag {
        return KafkaOmsorgsGrunnlag(
            omsorgsyter = Person(omsorgsyter),
            omsorgsAr = omsorgsAr,
            omsorgstype = omsorgstype,
            kjoreHash = "XXX",
            kilde = Kilde.BARNETRYGD,
            omsorgsSaker = listOf(
                KafkaOmsorgsSak(omsorgVedtakPeriode = omsorgsarbeidVedtak)
            )
        )
    }

    private fun createKafkaOmsorgsarbeid(
        fom: YearMonth,
        tom: YearMonth,
        omsorgsytere: List<String>,
        omsorgsmottakere: List<String>,
    ) = KafkaOmsorgVedtakPeriode(
        fom = fom,
        tom = tom,
        prosent = 100,
        omsorgsytere = omsorgsytere.map { Person(it) }.toSet(),
        omsorgsmottakere = omsorgsmottakere.map { Person(it) }.toSet(),
        landstilknytning = Landstilknytning.NASJONAL
    )


    companion object {
        private val omsorgsYter1 = PdlPerson(alleFnr = listOf(PdlFnr("1111", true)), fodselsAr = 1988)
        private val omsorgsYter2 = PdlPerson(alleFnr = listOf(PdlFnr("12345678910", true)), fodselsAr = 1990)
        private val omsorgsmottaker1 = PdlPerson(alleFnr = listOf(PdlFnr("2222", true)), fodselsAr = 2017)
        private val omsorgsmottaker2 = PdlPerson(alleFnr = listOf(PdlFnr("3333", true)), fodselsAr = 2018)

        private const val OMSORGS_AR_2020 = 2020
    }
}
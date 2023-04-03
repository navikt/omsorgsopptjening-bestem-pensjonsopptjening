package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import org.junit.jupiter.api.Assertions.*

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlFnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlService
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.*
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


@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsArbeidServiceTest {
    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var repository: OmsorgsarbeidSnapshotRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

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
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            omsorgsarbeid = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(2020, Month.JANUARY),
                    tom =  YearMonth.of(2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter1.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )

        val omsorgsArbeidInformasjon = omsorgsArbeidService.createSaveOmsorgasbeidsInformasjon(kafkaMessage)
        assertEquals(0, omsorgsArbeidInformasjon.relaterteOmsorgsarbeidSnapshot.size)
        assertEquals(
            omsorgsYter1.gjeldendeFnr,
            omsorgsArbeidInformasjon.omsorgsarbeidSnapshot.omsorgsyter.gjeldendeFnr.fnr
        )
    }

    @Test
    fun `Given a snapshot with other parties but no snapshot is stored in DB then return snapshot without other parties`() {
        val kafkaMessage = createKafkaMessage(
            omsorgsAr = OMSORGS_AR_2020,
            omsorgsyter = omsorgsYter1.gjeldendeFnr,
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            omsorgsarbeid = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(OMSORGS_AR_2020, Month.JANUARY),
                    tom =  YearMonth.of(OMSORGS_AR_2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter2.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )

        val omsorgsArbeidInformasjon = omsorgsArbeidService.createSaveOmsorgasbeidsInformasjon(kafkaMessage)

        assertEquals(0, omsorgsArbeidInformasjon.relaterteOmsorgsarbeidSnapshot.size)
        assertEquals(
            omsorgsYter1.gjeldendeFnr,
            omsorgsArbeidInformasjon.omsorgsarbeidSnapshot.omsorgsyter.gjeldendeFnr.fnr
        )
    }

    @Test
    fun `Given a snapshot with other parties that is stored in DB then return snapshot with other parties`() {
        val kafkaMessage1 = createKafkaMessage(
            omsorgsAr = OMSORGS_AR_2020,
            omsorgsyter = omsorgsYter1.gjeldendeFnr,
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            omsorgsarbeid = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(OMSORGS_AR_2020, Month.JANUARY),
                    tom =  YearMonth.of(OMSORGS_AR_2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter1.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )
        omsorgsArbeidService.createSaveOmsorgasbeidsInformasjon(kafkaMessage1)

        val kafkaMessage2 = createKafkaMessage(
            omsorgsAr = OMSORGS_AR_2020,
            omsorgsyter = omsorgsYter2.gjeldendeFnr,
            omsorgstype = OmsorgsarbeidsType.BARNETRYGD,
            omsorgsarbeid = listOf(
                createKafkaOmsorgsarbeid(
                    fom = YearMonth.of(OMSORGS_AR_2020, Month.JANUARY),
                    tom =  YearMonth.of(OMSORGS_AR_2020, Month.DECEMBER),
                    omsorgsytere = listOf(omsorgsYter1.gjeldendeFnr),
                    omsorgsmottakere = listOf(omsorgsmottaker1.gjeldendeFnr),
                )
            )
        )
        val omsorgsArbeidInformasjon = omsorgsArbeidService.createSaveOmsorgasbeidsInformasjon(kafkaMessage2)

        assertEquals(1, omsorgsArbeidInformasjon.relaterteOmsorgsarbeidSnapshot.size)
        assertEquals(omsorgsYter2.gjeldendeFnr, omsorgsArbeidInformasjon.omsorgsarbeidSnapshot.omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(omsorgsYter1.gjeldendeFnr, omsorgsArbeidInformasjon.relaterteOmsorgsarbeidSnapshot.first().omsorgsyter.gjeldendeFnr.fnr)
    }

    private fun createKafkaMessage(
        omsorgsAr: Int,
        omsorgsyter: String,
        omsorgstype: OmsorgsarbeidsType,
        omsorgsarbeid: List<OmsorgsArbeid>
    ): OmsorgsarbeidsSnapshot {
        return OmsorgsarbeidsSnapshot(
            omsorgsyter = Person(omsorgsyter),
            omsorgsAr = omsorgsAr,
            omsorgstype = omsorgstype,
            kjoreHash = "XXX",
            kilde = OmsorgsarbeidsKilde.BARNETRYGD,
            omsorgsArbeidSaker = listOf(
                OmsorgsArbeidSak(omsorgsarbedUtfort = omsorgsarbeid)
            )
        )
    }

    private fun createKafkaOmsorgsarbeid(
        fom: YearMonth,
        tom: YearMonth,
        omsorgsytere: List<String>,
        omsorgsmottakere: List<String>,
    ) = OmsorgsArbeid(
        fom = fom,
        tom = tom,
        prosent = 100,
        omsorgsytere = omsorgsytere.map { Person(it) }.toSet(),
        omsorgsmottaker = omsorgsmottakere.map { Person(it) }.toSet()
    )


    companion object {
        private val omsorgsYter1 = PdlPerson(alleFnr = listOf(PdlFnr("1111", true)), fodselsAr = 1988)
        private val omsorgsYter2 = PdlPerson(alleFnr = listOf(PdlFnr("12345678910", true)), fodselsAr = 1990)
        private val omsorgsmottaker1 = PdlPerson(alleFnr = listOf(PdlFnr("2222", true)), fodselsAr = 2017)
        private val omsorgsmottaker2 = PdlPerson(alleFnr = listOf(PdlFnr("3333", true)), fodselsAr = 2018)

        private const val OMSORGS_AR_2020 = 2020
    }
}
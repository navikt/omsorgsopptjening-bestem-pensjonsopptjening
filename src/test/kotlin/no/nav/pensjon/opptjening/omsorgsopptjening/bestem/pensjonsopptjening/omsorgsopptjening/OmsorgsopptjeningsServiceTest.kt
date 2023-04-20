package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToClass
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository.OmsorgsarbeidSnapshotRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlFnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.KafkaMessageType
import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjening
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Month
import java.time.YearMonth

@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
class OmsorgsopptjeningsServiceTest {

    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var omsorgsarbeidRepository: OmsorgsarbeidSnapshotRepository

    @Autowired
    private lateinit var omsorgsopptjeningsService: OmsorgsopptjeningsService

    @Autowired
    private lateinit var omsorgsopptjeningMockListener: OmsorgsopptjeningMockListener

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()
    }

    @Test
    fun test() {
        val omsorgsYter1 = personRepository.updatePerson(omsorgsYter1988)
        val omsorgsYter2 = personRepository.updatePerson(omsorgsYter1987)
        val omsorgsmottaker1 = personRepository.updatePerson(omsorgsmottaker2017)

        val omsorgsPeriode1 = createOmsorgsarbeidPeriode(
            fom = JANUAR_2020,
            tom = DESEMBER_2020,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter1, omsorgsYter2),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )

        val omsorgsPeriode2 = createOmsorgsarbeidPeriode(
            fom = JANUAR_2020,
            tom = DESEMBER_2020,
            prosent = PROSENT_50,
            omsorgsytere = listOf(omsorgsYter1, omsorgsYter2),
            omsorgsMottakere = listOf(omsorgsmottaker1)
        )

        val snapshotOmsorgsyter1 = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYter1,
            omsorgsArbeidSaker = listOf(OmsorgsSak(omsorgsvedtakPerioder = listOf(omsorgsPeriode1))),
            omsorgsAr = OMSORGS_AR_2020
        )

        val snapshotOmsorgsyter2 = creatOmsorgsArbeidSnapshot(
            omsorgsyter = omsorgsYter2,
            omsorgsArbeidSaker = listOf(OmsorgsSak(omsorgsvedtakPerioder = listOf(omsorgsPeriode2))),
            omsorgsAr = OMSORGS_AR_2020
        )

        omsorgsarbeidRepository.save(snapshotOmsorgsyter1)
        omsorgsopptjeningsService.behandlOmsorgsarbeid(snapshotOmsorgsyter1)

        omsorgsarbeidRepository.save(snapshotOmsorgsyter2)
        omsorgsopptjeningsService.behandlOmsorgsarbeid(snapshotOmsorgsyter2)


        val opptjening1 = getOmsorgopptjeningKafkaMessage()
        val opptjening2 = getOmsorgopptjeningKafkaMessage()
        val opptjening3 = getOmsorgopptjeningKafkaMessage()



        println("dssa")

    }

    private fun getOmsorgopptjeningKafkaMessage() = omsorgsopptjeningMockListener
        .removeFirstRecord(10, KafkaMessageType.OMSORGSOPPTJENING)!!
        .value()
        .mapToClass(OmsorgsOpptjening::class.java)


    private fun creatOmsorgsArbeidSnapshot(
        omsorgsyter: Person,
        omsorgsAr: Int,
        kjoreHashe: String = KJORE_HASHE,
        omsorgstype: Omsorgstype = TYPE_BARNETRYGD,
        kilde: Kilde = KILDE_BARNETRYGD,
        omsorgsArbeidSaker: List<OmsorgsSak> = listOf()
    ) =
        OmsorgsGrunnlag(
            omsorgsAr = omsorgsAr,
            kjoreHashe = kjoreHashe,
            omsorgsyter = omsorgsyter,
            omsorgstype = omsorgstype,
            kilde = kilde,
            omsorgsSaker = omsorgsArbeidSaker
        )

    private fun createOmsorgsarbeidPeriode(
        fom: YearMonth = JANUAR_2020,
        tom: YearMonth = DESEMBER_2020,
        prosent: Int,
        omsorgsytere: List<Person>,
        omsorgsMottakere: List<Person>
    ) =
        OmsorgsvedtakPeriode(
            fom = fom,
            tom = tom,
            prosent = prosent,
            omsorgsytere = omsorgsytere,
            omsorgsmottakere = omsorgsMottakere,
            landstilknytning = Landstilknytning.NASJONAL
        )


    companion object {
        private val omsorgsYter1988 = PdlPerson(alleFnr = listOf(PdlFnr("1111", true)), fodselsAr = 1988)
        private val omsorgsYter1987 = PdlPerson(alleFnr = listOf(PdlFnr("2222", true)), fodselsAr = 1987)
        private val omsorgsYter1986 = PdlPerson(alleFnr = listOf(PdlFnr("2222", true)), fodselsAr = 1986)
        private val omsorgsmottaker2017 = PdlPerson(alleFnr = listOf(PdlFnr("6666", true)), fodselsAr = 2017)
        private val omsorgsmottaker2018 = PdlPerson(alleFnr = listOf(PdlFnr("7777", true)), fodselsAr = 2018)

        private const val OMSORGS_AR_2020 = 2020
        private const val KJORE_HASHE = "xxxx"
        private val TYPE_BARNETRYGD = Omsorgstype.BARNETRYGD
        private val KILDE_BARNETRYGD = Kilde.BARNETRYGD

        private const val PROSENT_50 = 50
        private const val PROSENT_100 = 100

        private val JANUAR_2020 = YearMonth.of(2020, Month.JANUARY)
        private val DESEMBER_2020 = YearMonth.of(2020, Month.DECEMBER)
    }

}
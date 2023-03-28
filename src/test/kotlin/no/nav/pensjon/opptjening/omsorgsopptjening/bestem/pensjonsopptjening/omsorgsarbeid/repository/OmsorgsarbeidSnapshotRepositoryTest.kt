package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Status
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.kafka.test.context.EmbeddedKafka
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertNotNull


@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsarbeidSnapshotRepositoryTest {
    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var repository: OmsorgsarbeidSnapshotRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()
    }

    @Test
    fun `Persist omsorgsArbeidSnapshot`() {
        val omsorgsYter = personRepository.updatePerson(omsorgsYter1)
        val omsorgsmottaker = personRepository.updatePerson(omsorgsmottaker1)

        repository.save(
            creatOmsorgsArbeidSnapshot(
                omsorgsyter = omsorgsYter,
                omsorgsarbeidPerioder = listOf(
                    createOmsorgsArbeid(omsorgsyter = omsorgsYter, omsorgsmottakere = listOf(omsorgsmottaker))
                ),
            )
        )

        val grunnlagList = repository.findByPerson(omsorgsYter)

        assertEquals(1, grunnlagList.size)
        assertNotNull(grunnlagList.first().id)
        assertEquals(omsorgsYter.id, grunnlagList.first().omsorgsyter.id)
        assertEquals(OMSORGS_AR_2020, grunnlagList.first().omsorgsAr)
        assertEquals(TYPE_BARNETRYGD, grunnlagList.first().omsorgstype)
        assertEquals(KILDE_BARNETRYGD, grunnlagList.first().kilde)
        assertEquals(KJORE_HASHE, grunnlagList.first().kjoreHashe)

        assertEquals(1, grunnlagList.first().omsorgsarbeidSaker.size)
        val omsorgsarbeidSak: OmsorgsarbeidSak = grunnlagList.first().omsorgsarbeidSaker.first()

        assertEquals(1, omsorgsarbeidSak.omsorgsarbeidPerioder.size)
        val omsorgsarbeidPeriode: OmsorgsarbeidPeriode = omsorgsarbeidSak.omsorgsarbeidPerioder.first()

        assertNotNull(omsorgsarbeidPeriode.id)
        assertEquals(JANUAR_2020, omsorgsarbeidPeriode.fom)
        assertEquals(DESEMBER_2020, omsorgsarbeidPeriode.tom)
        assertEquals(PROSENT_100, omsorgsarbeidPeriode.prosent)
        assertEquals(omsorgsYter.id, omsorgsarbeidPeriode.omsorgsyter.id)
        assertEquals(1,omsorgsarbeidPeriode.omsorgsmottakere.size)
        assertEquals(omsorgsmottaker.id,omsorgsarbeidPeriode.omsorgsmottakere.first().id)
    }


    @Test
    fun `When saving unpersisted person Then throw exception`() {
        val omsorgsYter = personRepository.updatePerson(omsorgsYter1)
        val omsorgsmottaker = personRepository.updatePerson(omsorgsmottaker1)
        val unpersistedPerson =  Person(fodselsAr = 2000)

        val e1 = assertThrows<IllegalStateException> {
            repository.save(
                creatOmsorgsArbeidSnapshot(
                    omsorgsyter = unpersistedPerson,
                    omsorgsarbeidPerioder = listOf(
                        createOmsorgsArbeid(omsorgsyter = omsorgsYter, omsorgsmottakere = listOf(omsorgsmottaker))
                    ),
                )
            )
        }


        val e2 = assertThrows<IllegalStateException> {
            repository.save(
                creatOmsorgsArbeidSnapshot(
                    omsorgsyter = omsorgsYter,
                    omsorgsarbeidPerioder = listOf(
                        createOmsorgsArbeid(omsorgsyter = unpersistedPerson, omsorgsmottakere = listOf(omsorgsmottaker))
                    ),
                )
            )
        }


        val e3 = assertThrows<InvalidDataAccessApiUsageException> {
            repository.save(
                creatOmsorgsArbeidSnapshot(
                    omsorgsyter = omsorgsYter,
                    omsorgsarbeidPerioder = listOf(
                        createOmsorgsArbeid(omsorgsyter = omsorgsYter, omsorgsmottakere = listOf(unpersistedPerson))
                    ),
                )
            )
        }

        assertTrue(e1.message!!.contains("transient instance must be saved before current operation"))
        assertTrue(e2.message!!.contains("transient instance must be saved before current operation"))
        assertTrue(e3.message!!.contains("save the transient instance before flushing"))
    }

    private fun creatOmsorgsArbeidSnapshot(
        omsorgsyter: Person,
        omsorgsAr: Int = OMSORGS_AR_2020,
        kjoreHashe: String = KJORE_HASHE,
        omsorgstype: Omsorgstype = TYPE_BARNETRYGD,
        kilde: Kilde = KILDE_BARNETRYGD,
        omsorgsarbeidPerioder: List<OmsorgsarbeidPeriode>
    ) =

        OmsorgsarbeidSnapshot(
            omsorgsyter = omsorgsyter,
            omsorgsAr = omsorgsAr,
            kjoreHashe = kjoreHashe,
            omsorgstype = omsorgstype,
            kilde = kilde,
            omsorgsarbeidSaker = listOf(
                OmsorgsarbeidSak(
                    omsorgsarbeidPerioder = omsorgsarbeidPerioder
                )
            )
        )

    private fun createOmsorgsArbeid(
        fom: YearMonth = JANUAR_2020,
        tom: YearMonth = DESEMBER_2020,
        prosent: Int = PROSENT_100,
        omsorgsyter: Person,
        omsorgsmottakere: List<Person>
    ) = OmsorgsarbeidPeriode(
        fom = fom,
        tom = tom,
        prosent = prosent,
        omsorgsyter = omsorgsyter,
        omsorgsmottakere = omsorgsmottakere
    )

    companion object {
        private val omsorgsYter1 = PdlPerson(gjeldendeFnr = "1111", historiskeFnr = listOf(), fodselsAr = 1988)
        private val omsorgsmottaker1 = PdlPerson(gjeldendeFnr = "2222", historiskeFnr = listOf(), fodselsAr = 1989)

        private const val OMSORGS_AR_2020 = 2020
        private const val KJORE_HASHE = "dummyValue1"
        private val TYPE_BARNETRYGD = Omsorgstype.BARNETRYGD
        private val KILDE_BARNETRYGD = Kilde.BARNETRYGD

        private const val PROSENT_100 = 100
        private val JANUAR_2020 = YearMonth.of(2020, Month.JANUARY)
        private val DESEMBER_2020 = YearMonth.of(2020, Month.DECEMBER)
    }
}
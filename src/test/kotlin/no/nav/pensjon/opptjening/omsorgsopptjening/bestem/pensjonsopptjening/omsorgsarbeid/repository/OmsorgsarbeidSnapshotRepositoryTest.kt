package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.*
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlFnr
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertNotNull


@SpringBootTest(classes = [App::class])
@ActiveProfiles("no-kafka")
internal class OmsorgsarbeidSnapshotRepositoryTest {
    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var repository: OmsorgsarbeidSnapshotRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

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
        assertEquals(omsorgsYter.id, omsorgsarbeidPeriode.omsorgsytere.first().id)
        assertEquals(1,omsorgsarbeidPeriode.omsorgsmottakere.size)
        assertEquals(omsorgsmottaker.id,omsorgsarbeidPeriode.omsorgsmottakere.first().id)
    }

    @Test
    fun `Given omsorgsArbeidSnapshot override When save Then set old snapshot to historisk true`() {
        val omsorgsYter = personRepository.updatePerson(omsorgsYter1)
        val omsorgsmottaker1 = personRepository.updatePerson(omsorgsmottaker1)
        val omsorgsmottaker2 = personRepository.updatePerson(omsorgsmottaker2)

        repository.save(
            creatOmsorgsArbeidSnapshot(
                omsorgsyter = omsorgsYter,
                omsorgsarbeidPerioder = listOf(
                    createOmsorgsArbeid(omsorgsyter = omsorgsYter, omsorgsmottakere = listOf(omsorgsmottaker1))
                ),
            )
        )

        repository.save(
            creatOmsorgsArbeidSnapshot(
                omsorgsyter = omsorgsYter,
                omsorgsarbeidPerioder = listOf(
                    createOmsorgsArbeid(omsorgsyter = omsorgsYter, omsorgsmottakere = listOf(omsorgsmottaker2))
                ),
            )
        )

        val snapshotList = repository.find(
            omsorgsyter = omsorgsYter,
            omsorgsAr = OMSORGS_AR_2020,
            historisk = false
        )

        val oldSnapshotList = repository.find(
            omsorgsyter = omsorgsYter,
            omsorgsAr = OMSORGS_AR_2020,
            historisk = true
        )

        assertEquals(1, snapshotList.size)
        assertEquals(1, oldSnapshotList.size)
        assertFalse(snapshotList.first().historisk)
        assertTrue(oldSnapshotList.first().historisk)
        assertNotEquals(snapshotList.first().id, oldSnapshotList.first().id)
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


        val e2 = assertThrows<InvalidDataAccessApiUsageException> {
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
        assertTrue(e2.message!!.contains("save the transient instance before flushing"))
        assertTrue(e3.message!!.contains("save the transient instance before flushing"))
    }

    @Test
    fun `Given existing omsorgsArbeidSnapshot for more than one omsorgsyter When saving Then do not mutate other omsorgsyter snapshot`() {
        val omsorgsYter1 = personRepository.updatePerson(omsorgsYter1)
        val omsorgsYter2 = personRepository.updatePerson(omsorgsYter2)

        val omsorgsmottaker1 = personRepository.updatePerson(omsorgsmottaker1)
        val omsorgsmottaker2 = personRepository.updatePerson(omsorgsmottaker2)

        // Saving snapshot for omsorgsyter1
        repository.save(
            creatOmsorgsArbeidSnapshot(
                omsorgsyter = omsorgsYter1,
                omsorgsarbeidPerioder = listOf(
                    createOmsorgsArbeid(omsorgsyter = omsorgsYter1, omsorgsmottakere = listOf(omsorgsmottaker1))
                ),
            )
        )

        // Saving snapshot for omsorgsyter2
        repository.save(
            creatOmsorgsArbeidSnapshot(
                omsorgsyter = omsorgsYter2,
                omsorgsarbeidPerioder = listOf(
                    createOmsorgsArbeid(omsorgsyter = omsorgsYter2, omsorgsmottakere = listOf(omsorgsmottaker2))
                ),
            )
        )

        // override snapshot for omsorgsyter2
        repository.save(
            creatOmsorgsArbeidSnapshot(
                omsorgsyter = omsorgsYter2,
                omsorgsarbeidPerioder = listOf(
                    createOmsorgsArbeid(omsorgsyter = omsorgsYter2, omsorgsmottakere = listOf(omsorgsmottaker1))
                ),
            )
        )

        val snapshotListomsorgsYter1 = repository.find(
            omsorgsyter = omsorgsYter1,
            omsorgsAr = OMSORGS_AR_2020,
            historisk = false
        )

        val oldSnapshotListomsorgsYter1 = repository.find(
            omsorgsyter = omsorgsYter1,
            omsorgsAr = OMSORGS_AR_2020,
            historisk = true
        )

        // Validating historisk and not historisk snapshot for omsorgsyter1
        assertEquals(1, snapshotListomsorgsYter1.size)
        assertEquals("1111", snapshotListomsorgsYter1.first().omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(0, oldSnapshotListomsorgsYter1.size)
        assertFalse(snapshotListomsorgsYter1.first().historisk)

        val snapshotListOmsorgsyter2 = repository.find(
            omsorgsyter = omsorgsYter2,
            omsorgsAr = OMSORGS_AR_2020,
            historisk = false
        )

        val oldsnapshotListOmsorgsyter2 = repository.find(
            omsorgsyter = omsorgsYter2,
            omsorgsAr = OMSORGS_AR_2020,
            historisk = true
        )

        //Validating current snapshot for omsorgsyter2
        assertEquals(1, snapshotListOmsorgsyter2.size)
        assertFalse(snapshotListOmsorgsyter2.first().historisk)
        assertEquals("12345678910",snapshotListOmsorgsyter2.first().omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(1, snapshotListOmsorgsyter2.first().omsorgsarbeidSaker.first().omsorgsarbeidPerioder.first().omsorgsmottakere.size)
        assertEquals(omsorgsmottaker1.gjeldendeFnr.fnr, snapshotListOmsorgsyter2.first().omsorgsarbeidSaker.first().omsorgsarbeidPerioder.first().omsorgsmottakere.first().gjeldendeFnr.fnr)

        //Validating historic snapshot for omsorgsyter2
        assertEquals(1, oldsnapshotListOmsorgsyter2.size)
        assertTrue(oldsnapshotListOmsorgsyter2.first().historisk)
        assertEquals("12345678910",oldsnapshotListOmsorgsyter2.first().omsorgsyter.gjeldendeFnr.fnr)
        assertEquals(1, oldsnapshotListOmsorgsyter2.first().omsorgsarbeidSaker.first().omsorgsarbeidPerioder.first().omsorgsmottakere.size)
        assertEquals(omsorgsmottaker2.gjeldendeFnr.fnr, oldsnapshotListOmsorgsyter2.first().omsorgsarbeidSaker.first().omsorgsarbeidPerioder.first().omsorgsmottakere.first().gjeldendeFnr.fnr)
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
        omsorgsytere = listOf(omsorgsyter),
        omsorgsmottakere = omsorgsmottakere,
        landstilknytning = Landstilknytning.NASJONAL
    )

    companion object {
        private val omsorgsYter1 = PdlPerson(alleFnr = listOf(PdlFnr("1111", true)), fodselsAr = 1988)
        private val omsorgsYter2 = PdlPerson(alleFnr = listOf(PdlFnr("12345678910", true)), fodselsAr = 1990)
        private val omsorgsmottaker1 = PdlPerson(alleFnr = listOf(PdlFnr("2222", true)), fodselsAr = 2017)
        private val omsorgsmottaker2 = PdlPerson(alleFnr = listOf(PdlFnr("3333", true)), fodselsAr = 2018)

        private const val OMSORGS_AR_2020 = 2020
        private const val KJORE_HASHE = "dummyValue1"
        private val TYPE_BARNETRYGD = Omsorgstype.BARNETRYGD
        private val KILDE_BARNETRYGD = Kilde.BARNETRYGD

        private const val PROSENT_100 = 100
        private val JANUAR_2020 = YearMonth.of(2020, Month.JANUARY)
        private val DESEMBER_2020 = YearMonth.of(2020, Month.DECEMBER)
    }
}
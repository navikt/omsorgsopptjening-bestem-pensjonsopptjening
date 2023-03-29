package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class PersonRepositoryTest {
    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var fnrJpaRepository: FnrJpaRepository

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()
    }

    @Test
    fun `Given updating person not in db When updatePerson Then create new person`() {
        val pdlPerson = PdlPerson(
            gjeldendeFnr = "5555",
            historiskeFnr = listOf("6666", "7777"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(pdlPerson)
        val person = personRepository.findPersonByFnr("5555")

        assertNotNull(person)
        assertEquals(pdlPerson.gjeldendeFnr, person.gjeldendeFnr.fnr)
        assertEquals(pdlPerson.fodselsAr, person.fodselsAr)
        assertEquals(pdlPerson.historiskeFnr.size, person.historiskeFnr.size)
        assertTrue(person.historiskeFnr.any { pdlPerson.historiskeFnr.contains(it.fnr) })
    }

    @Test
    fun `Given updating saved person with more historiskeFnr When updatePerson with pdlPerson Then new fnrs should be added`() {
        val initialPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf(),
            fodselsAr = 2000
        )
        personRepository.updatePerson(initialPdlPerson)

        val updatedPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf("2222", "3333"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(updatedPdlPerson)

        val updatedPerson = personRepository.findPersonByFnr("1111")

        assertNotNull(updatedPerson)
        assertEquals(updatedPdlPerson.gjeldendeFnr, updatedPerson.gjeldendeFnr.fnr)
        assertEquals(updatedPdlPerson.fodselsAr, updatedPerson.fodselsAr)
        assertEquals(updatedPdlPerson.historiskeFnr.size, updatedPerson.historiskeFnr.size)
        assertContainsOnlySameFnrs(updatedPdlPerson, updatedPerson)
    }

    @Test
    fun `Given updating saved person with less historiskeFnr When updatePerson with pdlPerson Then remove historiskeFnr`() {
        val initialPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf("2222", "3333"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(initialPdlPerson)

        val updatedPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf(),
            fodselsAr = 2000
        )
        personRepository.updatePerson(updatedPdlPerson)

        val updatedPerson= personRepository.findPersonByFnr("1111")

        assertNotNull(updatedPerson)
        assertEquals(updatedPdlPerson.fodselsAr, updatedPerson.fodselsAr)
        assertEquals(updatedPdlPerson.gjeldendeFnr, updatedPerson.gjeldendeFnr.fnr)
        assertEquals(1, updatedPerson.alleFnr.size)
        assertOnlyFnrsFromPdlPersonExistsInDb(updatedPdlPerson)
    }

    @Test
    fun `Given saved person has only one historisk fnr in common with updated person When updatePerson with pdlPerson Then update person`() {
        val initialPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf("2222", "3333"),
            fodselsAr = 2000
        )
        val initialPersonId = personRepository.updatePerson(initialPdlPerson).id

        val updatedPdlPerson = PdlPerson(
            gjeldendeFnr = "4444",
            historiskeFnr = listOf("3333", "5555"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(updatedPdlPerson)

        val updatedPerson = personRepository.findPersonByFnr("4444")

        assertNotNull(updatedPerson)
        assertEquals(initialPersonId, updatedPerson.id)
        assertEquals(updatedPdlPerson.fodselsAr, updatedPerson.fodselsAr)
        assertEquals(updatedPdlPerson.gjeldendeFnr, updatedPerson.gjeldendeFnr.fnr)
        assertContainsOnlySameFnrs(updatedPdlPerson, updatedPerson)
        assertOnlyFnrsFromPdlPersonExistsInDb(updatedPdlPerson)
    }

    @Test
    fun `given updated fodselsAr When updatePerson with pdlPerson Then update fodselsAr`() {
        val initialPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf(),
            fodselsAr = 2000
        )
        personRepository.updatePerson(initialPdlPerson)

        val updatedPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf(),
            fodselsAr = 2010
        )
        personRepository.updatePerson(updatedPdlPerson)

        val updatedPerson = personRepository.findPersonByFnr("1111")

        assertNotNull(updatedPerson)
        assertEquals(updatedPdlPerson.fodselsAr, updatedPerson.fodselsAr)
    }

    @Test
    fun `given overlapping person When updatePerson with pdlPerson Then throw exception  `() {
        val pdlPerson1 = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf("2222", "3333"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(pdlPerson1).id

        val pdlPerson2 = PdlPerson(
            gjeldendeFnr = "4444",
            historiskeFnr = listOf("5555", "6666"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(pdlPerson2)

        val overlappingPdlPerson = PdlPerson(
            gjeldendeFnr = "4444",
            historiskeFnr = listOf("1111"),
            fodselsAr = 2000
        )
        assertThrows<DatabaseError> { personRepository.updatePerson(overlappingPdlPerson) }
    }

    private fun assertContainsOnlySameFnrs(pdlPerson: PdlPerson, person: Person) {
        val allFnrsPdl: Set<String> = pdlPerson.alleFnr()
        assertTrue(allFnrsPdl.all { person.identifiseresAv(it) }, "Alle fnr fra pdl var ikke i person")
        assertEquals(person.alleFnr.size, allFnrsPdl.size, "Det er flere fnr i person enn i pdlPerson")
    }

    private fun assertOnlyFnrsFromPdlPersonExistsInDb(pdlPerson: PdlPerson){
        val allFnrsInDb = fnrJpaRepository.findAll().toSet()
        val allFnrsFromPdl = (pdlPerson.historiskeFnr + pdlPerson.gjeldendeFnr).toSet()

        assertTrue(allFnrsFromPdl.all { allFnrsInDb.map {dbFnr ->  dbFnr.fnr }.contains(it)}, "Alle fnr fra pdl var ikke i database")
        assertEquals(allFnrsFromPdl.size, allFnrsInDb.size, "Det er flere fnr i db enn i pdlPerson")
    }
}




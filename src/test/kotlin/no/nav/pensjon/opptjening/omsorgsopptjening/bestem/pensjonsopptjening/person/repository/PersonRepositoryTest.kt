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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class PersonRepositoryTest {
    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    lateinit var personRepository: PersonRepository

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
        val person = personRepository.updatePerson(pdlPerson)

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

        val updatedPerson: Person = personRepository.fnrRepository.findPersonByFnr("1111")!!

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

        val personUpdated: Person = personRepository.fnrRepository.findPersonByFnr("1111")!!

        assertEquals(updatedPdlPerson.fodselsAr, personUpdated.fodselsAr)
        assertEquals(updatedPdlPerson.gjeldendeFnr, personUpdated.gjeldendeFnr.fnr)
        assertEquals(1, personUpdated.alleFnr.size)
    }

    @Test
    fun `Given saved person has only one historisk fnr in common with updated person When updatePerson with pdlPerson Then update in person`() {
        val initialPdlPerson = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf("2222", "3333"),
            fodselsAr = 2000
        )
        val initialPersonId = personRepository.updatePerson(initialPdlPerson).id

        val updatedPdlPerson = PdlPerson(
            gjeldendeFnr = "4444",
            historiskeFnr = listOf("3333","5555"),
            fodselsAr = 2000
        )
        personRepository.updatePerson(updatedPdlPerson)

        val personUpdated: Person = personRepository.fnrRepository.findPersonByFnr("4444")!!

        assertEquals(updatedPdlPerson.fodselsAr, personUpdated.fodselsAr)
        assertEquals(updatedPdlPerson.gjeldendeFnr, personUpdated.gjeldendeFnr.fnr)
        assertContainsOnlySameFnrs(updatedPdlPerson, personUpdated)
    }

    private fun assertContainsOnlySameFnrs(pdlPerson:PdlPerson, person: Person){
        val allFnrsPdl = (pdlPerson.historiskeFnr + pdlPerson.gjeldendeFnr).toSet()
        assertTrue(allFnrsPdl.all { person.identifiseresAv(it) }, "Alle fnr fra pdl var ikke i person")
        assertEquals(person.alleFnr.size, allFnrsPdl.size, "Det er flere fnr i person enn i pdlPerson")
    }
}




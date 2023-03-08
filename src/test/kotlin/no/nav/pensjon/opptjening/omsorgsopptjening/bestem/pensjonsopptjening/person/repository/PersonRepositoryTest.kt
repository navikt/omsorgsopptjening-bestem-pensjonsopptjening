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
        val person1 = personRepository.updatePerson(pdlPerson)
        assertEquals(pdlPerson.gjeldendeFnr, person1.gjeldendeFnr.fnr)
        assertEquals(pdlPerson.fodselsAr, person1.fodselsAr)
        assertEquals(pdlPerson.historiskeFnr.size, person1.historiskeFnr.size)
        assertTrue(person1.historiskeFnr.any { pdlPerson.historiskeFnr.contains(it.fnr) })
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

        val personUpdated: Person = personRepository.fnrRepository.findPersonByFnr("1111")!!
        assertEquals(updatedPdlPerson.gjeldendeFnr, personUpdated.gjeldendeFnr.fnr)
        assertEquals(updatedPdlPerson.fodselsAr, personUpdated.fodselsAr)
        assertEquals(updatedPdlPerson.historiskeFnr.size, personUpdated.historiskeFnr.size)
        assertTrue((updatedPdlPerson.historiskeFnr + updatedPdlPerson.gjeldendeFnr).all { personUpdated.identifiseresAv(it) })
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
        assertEquals(1, personUpdated.alleFnr.size)
        assertEquals(updatedPdlPerson.gjeldendeFnr, personUpdated.gjeldendeFnr.fnr)
    }
}




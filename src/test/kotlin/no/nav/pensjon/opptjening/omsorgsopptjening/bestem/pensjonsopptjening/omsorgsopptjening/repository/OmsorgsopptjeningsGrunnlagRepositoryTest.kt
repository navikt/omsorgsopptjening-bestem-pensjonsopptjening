package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Status
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.pdl.PdlPerson
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.repository.PersonRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.kafka.test.context.EmbeddedKafka
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@EmbeddedKafka(partitions = 1, topics = [OmsorgsarbeidListenerTest.OMSORGSOPPTJENING_TOPIC])
@SpringBootTest(classes = [App::class])
@Import(KafkaIntegrationTestConfig::class, OmsorgsopptjeningMockListener::class)
internal class OmsorgsopptjeningsGrunnlagRepositoryTest {

    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var grunnlagRepository: OmsorgsopptjeningsGrunnlagRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()
    }

    @Test
    fun `Given person with omsorgsopptjeningsGrunnlag when findByInvolvertePersoner then return omsorgsopptjeningsGrunnlag`() {
        val person = personRepository.updatePerson(person1)

        grunnlagRepository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = 2020,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person)
            )
        )

        val grunnlag = grunnlagRepository.findByInvolvertePersoner(person)

        assertEquals(1, grunnlag.size)
        assertEquals(1, grunnlag.first().involvertePersoner.size)
        assertEquals(person.gjeldendeFnr, grunnlag.first().involvertePersoner.first().gjeldendeFnr)
    }

    @Test
    fun `Given two persons in same OmsorgsopptjeningsGrunnlag then return same grunnlag for both persons `() {
        val person1 = personRepository.updatePerson(person1)
        val person2 = personRepository.updatePerson(person2)

        grunnlagRepository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = 2020,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person1, person2)
            )
        )


        val grunnlagPerson1 = grunnlagRepository.findByInvolvertePersoner(person1)
        val grunnlagPerson2 = grunnlagRepository.findByInvolvertePersoner(person2)

        assertEquals(1, grunnlagPerson1.size)
        assertEquals(1, grunnlagPerson2.size)
        assertEquals(2, grunnlagPerson1.first().involvertePersoner.size)
        assertEquals(grunnlagPerson1.first().id, grunnlagPerson2.first().id)
    }

    @Test
    fun `OmsorgsopptjeningsGrunnlagRepository should save unsaved persons`() {
        val e = assertThrows<InvalidDataAccessApiUsageException> {
            grunnlagRepository.save(
                OmsorgsopptjeningsGrunnlag(
                    omsorgsAr = 2020,
                    status = Status.TRENGER_INFORMASJON,
                    involvertePersoner = listOf(Person(fodselsAr = 2010, alleFnr = mutableSetOf()))
                )
            )
        }

        assertTrue(e.message!!.contains("save the transient instance before flushing"))
    }

    companion object {
        private val person1 = PdlPerson(gjeldendeFnr = "1111", historiskeFnr = listOf(), fodselsAr = 1988)
        private val person2 = PdlPerson(gjeldendeFnr = "2222", historiskeFnr = listOf(), fodselsAr = 1989)
    }
}
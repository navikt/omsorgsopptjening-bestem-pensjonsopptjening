package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.repository

import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.App
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.KafkaIntegrationTestConfig
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.OmsorgsopptjeningMockListener
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.PostgresqlTestContainer
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.listener.OmsorgsarbeidListenerTest
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.OmsorgsopptjeningsGrunnlag
import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.model.Status
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

@SpringBootTest(classes = [App::class])
@ActiveProfiles("no-kafka")
internal class OmsorgsopptjeningsGrunnlagRepositoryTest {

    private val dbContainer = PostgresqlTestContainer.instance

    @Autowired
    private lateinit var repository: OmsorgsopptjeningsGrunnlagRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @BeforeEach
    fun clearDb() {
        dbContainer.removeDataFromDB()
    }

    @Test
    fun `Given saved omsorgsopptjeningsGrunnlag W hen findBy then return omsorgsopptjeningsGrunnlag`() {
        val person = personRepository.updatePerson(person1)

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person)
            )
        )

        val grunnlag = repository.findBy(personer = listOf(person), omsorgsAr = `2020`)

        assertEquals(1, grunnlag.size)
        assertEquals(1, grunnlag.first().involvertePersoner.size)
        assertEquals(person.gjeldendeFnr, grunnlag.first().involvertePersoner.first().gjeldendeFnr)
    }

    @Test
    fun `Given saved OmsorgsopptjeningsGrunnlag related to two persons When findBy Then return same grunnlag for both persons`() {
        val person1 = personRepository.updatePerson(person1)
        val person2 = personRepository.updatePerson(person2)

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person1, person2)
            )
        )

        val grunnlagPerson1 = repository.findBy(personer = listOf(person1), omsorgsAr = `2020`)
        val grunnlagPerson2 = repository.findBy(personer = listOf(person2), omsorgsAr = `2020`)

        assertEquals(1, grunnlagPerson1.size)
        assertEquals(1, grunnlagPerson2.size)
        assertEquals(2, grunnlagPerson1.first().involvertePersoner.size)
        assertEquals(grunnlagPerson1.first().id, grunnlagPerson2.first().id)
    }

    @Test
    fun `When overriding grunnlag Then set old to historisk`() {
        val person = personRepository.updatePerson(person1)

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person)
            )
        )

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.FERDIG_BEHANDLET,
                involvertePersoner = listOf(person)
            )
        )

        val grunnlag = repository.findBy(personer = listOf(person), omsorgsAr = `2020`, historisk = false)

        assertEquals(1, grunnlag.size)
        assertEquals(1, grunnlag.first().involvertePersoner.size)
        assertEquals(person.gjeldendeFnr, grunnlag.first().involvertePersoner.first().gjeldendeFnr)
        assertFalse(grunnlag.first().historisk)

        val oldGrunnlag = repository.findBy(personer = listOf(person), omsorgsAr = `2020`, historisk = true)
        assertEquals(1, oldGrunnlag.size)
        assertEquals(1, oldGrunnlag.first().involvertePersoner.size)
        assertEquals(person.gjeldendeFnr, oldGrunnlag.first().involvertePersoner.first().gjeldendeFnr)
        assertTrue(oldGrunnlag.first().historisk)
    }

    @Test
    fun `When overriding grunnlag for two persons Then set old to historisk`() {
        val person1 = personRepository.updatePerson(person1)
        val person2 = personRepository.updatePerson(person2)

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person1, person2)
            )
        )

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.FERDIG_BEHANDLET,
                involvertePersoner = listOf(person1, person2)
            )
        )


        val grunnlag = repository.findBy(personer = listOf(person1, person2), omsorgsAr = `2020`, historisk = false)
        assertEquals(1, grunnlag.size)
        assertEquals(2, grunnlag.first().involvertePersoner.size)
        assertTrue(grunnlag.first().involvertePersoner.any{it.erSammePerson(person1)})
        assertTrue(grunnlag.first().involvertePersoner.any{it.erSammePerson(person2)})
        assertFalse(grunnlag.first().historisk)

        val oldGrunnlag = repository.findBy(personer = listOf(person1, person2), omsorgsAr = `2020`, historisk = true)
        assertEquals(1, oldGrunnlag.size)
        assertEquals(2, oldGrunnlag.first().involvertePersoner.size)
        assertTrue(oldGrunnlag.first().involvertePersoner.any{it.erSammePerson(person1)})
        assertTrue(oldGrunnlag.first().involvertePersoner.any{it.erSammePerson(person2)})
        assertTrue(oldGrunnlag.first().historisk)
    }

    @Test
    fun `Given saved grunnlag for two different years When findBy Then historisk should be false`() {
        val person = personRepository.updatePerson(person1)

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2019`,
                status = Status.FERDIG_BEHANDLET,
                involvertePersoner = listOf(person)
            )
        )

        repository.save(
            OmsorgsopptjeningsGrunnlag(
                omsorgsAr = `2020`,
                status = Status.TRENGER_INFORMASJON,
                involvertePersoner = listOf(person)
            )
        )

        val grunnlag2019 = repository.findBy(personer = listOf(person), omsorgsAr = `2019`, historisk = false)
        val grunnlag2020 = repository.findBy(personer = listOf(person), omsorgsAr = `2020`, historisk = false)

        assertEquals(1, grunnlag2019.size)
        assertEquals(1, grunnlag2020.size)
        assertEquals(`2019`, grunnlag2019.first().omsorgsAr)
        assertEquals(`2020`, grunnlag2020.first().omsorgsAr)
        assertFalse(grunnlag2019.first().historisk)
        assertFalse(grunnlag2020.first().historisk)
    }

    @Test
    fun `OmsorgsopptjeningsGrunnlagRepository should not save unsaved persons`() {
        val e = assertThrows<InvalidDataAccessApiUsageException> {
            repository.save(
                OmsorgsopptjeningsGrunnlag(
                    omsorgsAr = `2020`,
                    status = Status.TRENGER_INFORMASJON,
                    involvertePersoner = listOf(Person(fodselsAr = 2010, alleFnr = mutableSetOf()))
                )
            )
        }

        assertTrue(e.message!!.contains("save the transient instance before flushing"))
    }

    companion object {
        private val person1 = PdlPerson(alleFnr = listOf(PdlFnr("1111", true)), fodselsAr = 1988)
        private val person2 = PdlPerson(alleFnr = listOf(PdlFnr("2222", true)), fodselsAr = 1989)

        private const val `2020` = 2020
        private const val `2019` = 2019
    }
}
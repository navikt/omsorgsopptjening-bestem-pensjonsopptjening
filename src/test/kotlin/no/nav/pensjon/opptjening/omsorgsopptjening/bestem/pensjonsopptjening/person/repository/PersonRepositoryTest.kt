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
        dbContainer.removeDataFroDB()
    }

    @Test
    fun `Given updating saved person When find person by fnr Then new fnrs should be added`() {
        val pdlPerson1 = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf(),
            fodselsAr = 2000
        )
        val person1 = personRepository.updatePerson(pdlPerson1)
        assertEquals(pdlPerson1.gjeldendeFnr, person1.gjeldendeFnr.fnr)
        assertEquals(pdlPerson1.fodselsAr, person1.fodselsAr)
        assertEquals(pdlPerson1.historiskeFnr.size, person1.historiskeFnr.size)

        val pdlPersonUpdated = PdlPerson(
            gjeldendeFnr = "1111",
            historiskeFnr = listOf("2222", "3333"),
            fodselsAr = 2000
        )

        personRepository.updatePerson(pdlPersonUpdated)

        val personUpdated: Person = personRepository.fnrRepository.findPersonByFnr("1111")!!
        assertEquals(pdlPersonUpdated.gjeldendeFnr, personUpdated.gjeldendeFnr.fnr)
        assertEquals(pdlPersonUpdated.fodselsAr, personUpdated.fodselsAr)
        assertEquals(pdlPersonUpdated.historiskeFnr.size, personUpdated.historiskeFnr.size)
        assertTrue((pdlPersonUpdated.historiskeFnr + pdlPersonUpdated.gjeldendeFnr).all {
            personUpdated.identifiseresAv(
                it
            )
        })
    }
}



